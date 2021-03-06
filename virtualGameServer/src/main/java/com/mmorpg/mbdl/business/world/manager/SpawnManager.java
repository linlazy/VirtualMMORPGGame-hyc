package com.mmorpg.mbdl.business.world.manager;

import com.mmorpg.mbdl.business.object.creator.AbstractObjectCreator;
import com.mmorpg.mbdl.business.object.creator.ObjectCreatorManager;
import com.mmorpg.mbdl.business.object.model.AbstractVisibleSceneObject;
import com.mmorpg.mbdl.business.world.resource.BornRes;
import com.mmorpg.mbdl.business.world.resource.SceneObjectAttrRes;
import com.mmorpg.mbdl.business.world.scene.model.Scene;
import com.mmorpg.mbdl.framework.resource.exposed.IStaticRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 怪物出生管理器
 *
 * @author Sando Geek
 * @since v1.0 2018/12/13
 **/
@Component
public class SpawnManager {
    @Autowired
    protected IStaticRes<Integer, SceneObjectAttrRes> sceneObjectAttrResMap;
    @Autowired
    private IStaticRes<Integer, BornRes> bornResMap;
    @Autowired
    private ObjectCreatorManager objectCreatorManager;

    /**
     * 让所有怪物出生到相应的场景
     * @param scenes
     */
    public void spawnAll(Collection<Scene> scenes){
        scenes.forEach(scene -> {
            int sceneId = scene.getSceneId();
            bornResMap.get(sceneId).getBornDataList().forEach(bornData -> {
                SceneObjectAttrRes sceneObjectAttrRes = sceneObjectAttrResMap.get(bornData.getObjectKey());
                AbstractObjectCreator creator = objectCreatorManager.getCreatorByObjectType(sceneObjectAttrRes.getObjectType());
                if (creator == null) {
                    throw new RuntimeException(String.format("对象类型[%s]没有相应的AbstractObjectCreator",sceneObjectAttrRes.getObjectType()));
                }
                AbstractVisibleSceneObject visibleSceneObject
                        = creator.create(sceneId, bornData);
                scene.appearInScene(visibleSceneObject);
            });

        });
    }
}
