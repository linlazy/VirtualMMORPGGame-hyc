package com.mmorpg.mbdl.business.object.model;

import com.mmorpg.mbdl.business.role.manager.PropManager;
import com.mmorpg.mbdl.business.role.model.prop.PropType;

/**
 * 抽象生物
 *
 * @author Sando Geek
 * @since v1.0 2018/12/11
 **/
public abstract class AbstractCreature extends AbstractVisibleSceneObject {
    protected PropManager propManager;

    public AbstractCreature(Long objectId, String name) {
        super(objectId, name);
        this.propManager = new PropManager(this);
        propManager.getPropTreeByType(PropType.CURRENT_HP);
        propManager.getPropTreeByType(PropType.CURRENT_MP);
        propManager.getPropTreeByType(PropType.MAX_HP);
        propManager.getPropTreeByType(PropType.MAX_MP);
        propManager.getPropTreeByType(PropType.ATTACK);
        propManager.getPropTreeByType(PropType.DEFENCE);
        init();
    }

    /**
     * 进一步初始化各项属性
     */
    protected abstract void init();

    public PropManager getPropManager() {
        return propManager;
    }

    public AbstractCreature setPropManager(PropManager propManager) {
        this.propManager = propManager;
        return this;
    }

}
