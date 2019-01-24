package com.mmorpg.mbdl.framework.storage.config.JetCache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheGetResult;
import com.google.common.base.Preconditions;
import com.mmorpg.mbdl.framework.common.utils.JsonUtil;
import com.mmorpg.mbdl.framework.storage.annotation.JetCacheConfig;
import com.mmorpg.mbdl.framework.storage.core.AbstractEntity;
import com.mmorpg.mbdl.framework.storage.core.EntityCreator;
import com.mmorpg.mbdl.framework.storage.core.IStorage;
import com.mmorpg.mbdl.framework.thread.task.DelayedTask;
import com.mmorpg.mbdl.framework.thread.task.TaskDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 使用JetCache的IStorage默认实现类
 *
 * @author Sando Geek
 * @since v1.0
 **/
@NoRepositoryBean
public class StorageJetCache <PK extends Serializable &Comparable<PK>,E extends AbstractEntity<PK>> extends SimpleJpaRepository<E,PK>
        implements IStorage<PK,E> {
    private static final Logger logger = LoggerFactory.getLogger(StorageJetCache.class);
    private Cache<PK,E> cache;
    /** {@link JetCacheConfig#delay()} */
    private int delay;
    /** 代理对象 */
    private IStorage<PK,E> proxy;


    public StorageJetCache(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public E create(E entity) {
        Preconditions.checkNotNull(entity,"entity不能为null");
        CacheGetResult<E> cacheGetResult = cache.GET(entity.getId());
        if (cacheGetResult.isSuccess()){
            E entityFromCache = cacheGetResult.getValue();
            // 缓存中缓存了其null值，说明数据库中没有，直接存库
            if (entityFromCache == null) {
                return insertOrUpdate(entity);
            }
            // 缓存中有，说明数据库中也有，那么不应该创建，抛出异常
            else {
                throw new EntityExistsException("数据库中已存在该实体，考虑使用update?");
            }
        }
        // 缓存中没有成功获取就查数据库
        else {
            if (exists(entity.getId())){
                throw new EntityExistsException("数据库中已存在该实体，考虑使用update?");
            }else {
                return insertOrUpdate(entity);
            }
        }
    }

    private E insertOrUpdate(E entity){
        PK id = entity.getId();
        E entitySaved = saveAndFlush(entity);
        cache.put(id,entitySaved);
        return entitySaved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public E update(E entity) {
        PK id = entity.getId();
        Preconditions.checkNotNull(id,"id不能为null");
        CacheGetResult<E> cacheGetResult = cache.GET(id);
        if (cacheGetResult.isSuccess()){
            E entityFromCache = cacheGetResult.getValue();
            // 缓存中缓存了其null值，说明数据库中没有，直接存库
            if (entityFromCache == null) {
                throw new EntityNotFoundException("数据库中不存在该实体，先create一下？");
            }
            // 缓存中有，说明数据库中也有
            else {
                return executeUpdate(entity);
            }
        }else {
            if (exists(entity.getId())){
                return executeUpdate(entity);
            }else {
                throw new EntityNotFoundException("数据库中不存在该实体，先create一下？");
            }
        }
    }

    private E executeUpdate(E entity) {
        AtomicReference<E> reference = new AtomicReference<>(null);
        // 不能创建合并更新任务说明此实体已有此类任务
        // if (!entity.getCanCreateMergeUpdateTask().get() && entity.getMergeUpdateTaskFutureAtomic().get()!=null) {
            entity.getMergeUpdateTaskFutureAtomic().updateAndGet((prev) -> {
                AtomicReference<ScheduledFuture> mergeUpdateTaskFutureAtomic = entity.getMergeUpdateTaskFutureAtomic();
                if (prev!=null) {
                    prev.cancel(false);
                    if (prev.isCancelled()) {
                        try {
                            reference.set(insertOrUpdate(entity));
                        } finally {
                            mergeUpdateTaskFutureAtomic.set(null);
                        }
                    } else {
                        return prev;
                    }
                } else {
                    try {
                        reference.set(insertOrUpdate(entity));
                    } finally {
                        mergeUpdateTaskFutureAtomic.set(null);
                    }
                }
                return mergeUpdateTaskFutureAtomic.get();
            });
        // } else {
        //     reference.set(insertOrUpdate(entity));
        // }
        return reference.get();
    }

    @Override
    public void mergeUpdate(E entity) {
        if (this.delay == 0) {
            proxy.update(entity);
            return;
        }
        // else if (!entity.getCanCreateMergeUpdateTask().compareAndSet(true, false)) {
        //     return;
        // }
        entity.getMergeUpdateTaskFutureAtomic().updateAndGet(prev -> {
            if (prev != null) {
                return prev;
            } else {
                ScheduledFuture<?> scheduledFuture = TaskDispatcher.getInstance().dispatch(new DelayedTask(null, delay, TimeUnit.SECONDS) {
                    @Override
                    public String taskName() {
                        return String.format("合并更新实体[%s]：%s", entity.getClass().getSimpleName(), JsonUtil.object2String(entity));
                    }

                    @Override
                    public void execute() {
                        try {
                            entity.getMergeUpdateTaskFutureAtomic().set(null);
                            proxy.update(entity);
                        } finally {

                            // entity.getCanCreateMergeUpdateTask().set(true);
                        }
                    }
                }.setLogOrNot(true).setMaxExecuteTime(30, TimeUnit.MILLISECONDS), true);
                return scheduledFuture;
            }
        });
    }

    @Override
    public E get(PK id) {
        Preconditions.checkNotNull(id,"id不能为null");
        CacheGetResult<E> cacheGetResult = cache.GET(id);
        if (cacheGetResult.isSuccess()){
            E entityFromCache = cacheGetResult.getValue();
            return entityFromCache;
        }else {
            E entity = findOne(id);
            // 不管是null还是实体都缓存起来
            cache.put(id,entity);
            return entity;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public E getOrCreate(PK id, EntityCreator<PK, E> entityCreator) {
        Preconditions.checkNotNull(id,"id不能为null");
        CacheGetResult<E> cacheGetResult = cache.GET(id);
        if (cacheGetResult.isSuccess()){
            return cacheGetResult.getValue();
        }else {
            E entityCreated = entityCreator.create(id);
            return insertOrUpdate(entityCreated);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public E remove(PK id) {
        Preconditions.checkNotNull(id,"id不能为null");
        CacheGetResult<E> cacheGetResult = cache.GET(id);
        if (cacheGetResult.isSuccess()){
            E entity = cacheGetResult.getValue();
            // 缓存中缓存了其null值，说明数据库中没有，直接返回null
            if (entity == null) {
                return entity;
            }
            // 缓存中有，说明数据库中也有
            else {
                delete(id);
                cache.put(id,null);
                return entity;
            }
        }else {
            // 缓存没有直接查库
            E entity = findOne(id);
            if (entity != null){
                delete(id);
                cache.put(id,null);
            }
            return entity;
        }
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setProxy(IStorage<PK, E> proxy) {
        this.proxy = proxy;
    }

    public void setCache(Cache<PK, E> cache) {
        this.cache = cache;
    }
}
