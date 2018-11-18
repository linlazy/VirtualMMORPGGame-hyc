package com.mmorpg.mbdl.framework.storage.core;

import com.github.xiaolyuh.annotation.FirstCache;
import com.github.xiaolyuh.annotation.SecondaryCache;
import com.github.xiaolyuh.cache.Cache;
import com.github.xiaolyuh.manager.CacheManager;
import com.github.xiaolyuh.setting.FirstCacheSetting;
import com.github.xiaolyuh.setting.LayeringCacheSetting;
import com.github.xiaolyuh.setting.SecondaryCacheSetting;
import com.google.common.base.Preconditions;
import com.mmorpg.mbdl.framework.storage.annotation.CacheConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * IStorage的默认实现类
 * @param <PK> 主键类型
 * @param <E> 实体类型
 * @author sando
 */
@NoRepositoryBean
public class StorageMySql<PK extends Serializable &Comparable<PK>,E extends IEntity<PK>> extends SimpleJpaRepository<E,PK>
        implements IStorage<PK,E> {
    private static final Logger logger = LoggerFactory.getLogger(StorageMySql.class);

    /** IStorageBeanPostProcessor中注入 */
    private CacheManager cacheManager;
    /** 泛型E的实际类型 */
    private Class<? extends IEntity> eClazz;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void seteClazz(Class<? extends IEntity> eClazz) {
        this.eClazz = eClazz;
    }

    public StorageMySql(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    /**
     * 根据entityClass获取缓存
     * @return Cache
     */
    private Cache getCache(){
        CacheConfig cacheConfig = eClazz.getAnnotation(CacheConfig.class);
        Preconditions.checkNotNull(cacheConfig,"实体类[%s]没有使用@CacheConfig配置缓存",eClazz.getSimpleName());
        String cacheName = cacheConfig.cacheName();
        if (StringUtils.isEmpty(cacheName)){
            cacheName = eClazz.getSimpleName();
        }
        FirstCache firstCache = cacheConfig.firstCache();
        SecondaryCache secondaryCache = cacheConfig.secondaryCache();
        FirstCacheSetting firstCacheSetting = new FirstCacheSetting(firstCache.initialCapacity(), firstCache.maximumSize(),
                firstCache.expireTime(), firstCache.timeUnit(), firstCache.expireMode());

        SecondaryCacheSetting secondaryCacheSetting = new SecondaryCacheSetting(secondaryCache.expireTime(),
                secondaryCache.preloadTime(), secondaryCache.timeUnit(), secondaryCache.forceRefresh());
        LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting(firstCacheSetting, secondaryCacheSetting, cacheConfig.depict());
        // 通过cacheName和缓存配置获取Cache
        Cache cache = cacheManager.getCache(cacheName, layeringCacheSetting);
        return cache;
    }

    /**
     * 因为调用自身的saveAndFlush时，aop无法完成切面增强，所以这里必须加上@Transactional
     * @param id 主键
     * @param entityCreator 实体创建器
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public E createOrUpdate(PK id, EntityCreator<PK, E> entityCreator) {
        E entity = entityCreator.create(id);
        Cache cache = getCache();
        E entityAfterSave = this.saveAndFlush(entity);
        cache.put(id,entity);
        return entityAfterSave;
    }

    @Override
    public E getFromCache(PK id,Class<? extends IEntity> eClazz){
        Cache cache = getCache();
        return (E)cache.get(id,eClazz);
    }

    @Override
    public E get(PK id) {
        return null;
    }

    @Override
    public E getOrCreate(PK id, EntityCreator<PK, E> entityCreator) {
        return null;
    }

    // @Override
    // public E update(E entity) {
    //     return null;
    // }

    @Override
    public E remove(PK id) {
        return null;
    }

    @Override
    public void invalidate(PK id) {

    }
}
