package com.mmorpg.mbdl.business.container.model;

/**
 * 容器
 *
 * @author Sando Geek
 * @since v1.0 2019/1/15
 **/
public class Container {
    /** 背包类型 */
    private ContainerType containerType;

    public Container setContainerType(ContainerType containerType) {
        this.containerType = containerType;
        return this;
    }
}
