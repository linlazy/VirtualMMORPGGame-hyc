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
        propManager.getOrCreateTree(PropType.CURRENT_HP);
        propManager.getOrCreateTree(PropType.CURRENT_MP);
        propManager.getOrCreateTree(PropType.MAX_HP);
        propManager.getOrCreateTree(PropType.MAX_MP);
        propManager.getOrCreateTree(PropType.ATTACK);
        propManager.getOrCreateTree(PropType.DEFENCE);
    }

    public void fullHP() {
        propManager.getPropTreeByType(PropType.CURRENT_HP).setRootNodeValue(propManager.getPropValueOf(PropType.MAX_HP));
    }

    public void fullMP() {
        propManager.getPropTreeByType(PropType.CURRENT_MP).setRootNodeValue(propManager.getPropValueOf(PropType.MAX_MP));
    }

    /**
     * 当前血量变更
     * @param n 改变量，正数表示添加，负数表示减少
     */
    public void changeHp(long n){
        if (n==0) {
            return;
        }
        propManager.getPropTreeByType(PropType.CURRENT_HP).addRootNodeValue(n);
    }

    /**
     * 当前蓝量变更
     * @param n 改变量，正数表示添加，负数表示减少
     */
    public void changeMp(long n) {
        if (n==0) {
            return;
        }
        propManager.getPropTreeByType(PropType.CURRENT_MP).addRootNodeValue(n);
    }

    public PropManager getPropManager() {
        return propManager;
    }
}
