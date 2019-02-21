package com.mmorpg.mbdl.business.role.model.prop;

import com.google.common.base.MoreObjects;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 属性树
 *
 * @author Sando Geek
 * @since v1.0 2019/1/21
 **/
public class PropTree {
    /** 该类型属性的属性值（此值等于属性树所有节点的值之和） */
    private long propValue;
    /**
     * 最大属性值
     */
    Long maxValue;
    /** 属性树根节点 */
    private PropNode rootNode = new PropNode(this, doGetPropValue(),"根节点");
    /**
     * 属性树读写锁
     */
    protected ReentrantReadWriteLock treeRWLock = new ReentrantReadWriteLock();

    ReentrantReadWriteLock getTreeRWLock() {
        return treeRWLock;
    }

    /**
     * 获取属性值（此值等于属性树所有节点的值之和）
     * @return
     */
    public long getPropValue() {
        try {
            treeRWLock.readLock().lock();
            return doGetPropValue();
        } finally {
            treeRWLock.readLock().unlock();
        }
    }

    /**
     * 获取属性树根节点的值
     * @return
     */
    public long getRootNodeValue() {
        return rootNode.getValue();
    }

    /**
     * 修改属性树根节点的值
     * @param newValue
     */
    public void setRootNodeValue(long newValue) {
        rootNode.set(newValue);
    }

    public long addRootNodeValue(long delta) {
        return rootNode.addAndGet(delta);
    }

    public PropNode getOrCreateChild(String name) {
        return rootNode.getOrCreateChild(name);
    }

    public void removeChild(String name) {
        rootNode.removeChild(name);
    }

    public PropNode getChild(String name) {
        return rootNode.getChild(name);
    }

    /**
     * 修改属性的真实值
     * @param newValue
     * @return
     */
    void setPropValue(long newValue) {
        if (newValue < 0 ) {
            throw new RuntimeException("属性值不能为负值");
        } else if (maxValue!=null && newValue > maxValue) {
            doSetPropValue(maxValue);
        }
        doSetPropValue(newValue);
    }

    public PropTree setMaxValue(Long maxValue) {
        try {
            treeRWLock.writeLock().lock();
            this.maxValue = maxValue;
        } finally {
            treeRWLock.writeLock().unlock();
        }
        return this;
    }

    void addPropValue(long delta) {
        setPropValue(doGetPropValue()+delta);
    }

    protected long doGetPropValue() {
        return propValue;
    }

    protected void doSetPropValue(long newValue) {
        this.propValue = newValue;
    }

    /**
     * 覆写以便debug
     * @return
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("属性值", getPropValue())
                .toString();
    }
}
