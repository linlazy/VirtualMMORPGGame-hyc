package com.mmorpg.mbdl.framework.resource.facade;

import java.util.Collection;
import java.util.List;

/**
 * 静态资源访问接口
 *
 * @author Sando Geek
 * @since v1.0
 **/
public interface IStaticRes<K,V> {

    /**
     * 根据键获取V类型的对象，相当于调用get(K key,true)
     * @param key 资源键的值
     * @return
     */
    V get(K key);

    /**
     * 根据键获取V类型的对象
     * @param key 资源主键值
     * @param throwExceptionNotExist 资源不存在时是否抛出异常
     * @return if存在，资源对象 if不存在 if throwExceptionNotExist==true else null
     * @throws IllegalArgumentException
     */
    V get(K key,boolean throwExceptionNotExist);

    /**
     * 根据唯一值字段获取V类型的对象
     * @param name 唯一值字段名
     * @param uniqueValue 唯一值字段值
     * @return V类型的对象
     */
    V getByUnique(String name,Object uniqueValue);

    /**
     * 根据索引获取资源对象的集合
     * @param name 索引字段名
     * @param indexValue  索引值
     * @return 资源对象的集合
     */
    List<V> getByIndex(String name, Object indexValue);

    /**
     * 当前key是否有对应的值
     * @param key
     * @return 有 true 没有 false
     */
    boolean contains(K key);

    /**
     * 获取全部静态资源对象
     * @return 静态资源对象集合
     */
    Collection<V> values();

    /**
     * 资源对象的数量
     * @return 资源对象的数量
     */
    int size();
}
