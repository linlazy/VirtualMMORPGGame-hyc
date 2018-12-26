package com.mmorpg.mbdl.bussiness.world.manager;

import com.mmorpg.mbdl.bussiness.object.model.AbstractVisibleSceneObject;
import com.mmorpg.mbdl.bussiness.object.model.Role;
import com.mmorpg.mbdl.bussiness.role.entity.RoleEntity;
import com.mmorpg.mbdl.bussiness.world.resource.SceneRes;
import com.mmorpg.mbdl.bussiness.world.scene.model.Scene;
import com.mmorpg.mbdl.framework.resource.exposed.IStaticRes;
import com.mmorpg.mbdl.framework.storage.core.IStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 场景管理器
 *
 * @author Sando Geek
 * @since v1.0 2018/12/21
 **/
@Component
public class SceneManager {
    private static SceneManager self;
    private Map<Integer, Scene> sceneId2SceneMap = new HashMap<>(16);
    @Autowired
    private IStaticRes<Integer, SceneRes> id2Scene;
    @Autowired
    private IStorage<Long, RoleEntity> roleEntityIStorage;

    @PostConstruct
    private void init() {
        self = this;
        // 初始化场景
        id2Scene.values().forEach(sceneRes -> {
            Scene scene = new Scene().setName(sceneRes.getName())
                    .setSceneId(sceneRes.getSceneId());
            sceneId2SceneMap.put(scene.getSceneId(),scene);
        });
        // TODO 初始化怪物
    }

    public static SceneManager getInstance() {
        return self;
    }

    public Scene getSceneBySceneId(int sceneId){
        return sceneId2SceneMap.get(sceneId);
    }

    /**
     * 将可见物切换到场景id为sceneId的场景
     * @param visibleSceneObject 可见物
     * @param sceneId 场景id
     */
    public void switchToSceneId(AbstractVisibleSceneObject visibleSceneObject,int sceneId){
        getSceneBySceneId(visibleSceneObject.getSceneId()).disappearInScene(visibleSceneObject);
        visibleSceneObject.setSceneId(sceneId);
        if (visibleSceneObject instanceof Role){
            Role role = (Role) visibleSceneObject;
            role.getRoleEntity().setSceneId(sceneId);
            roleEntityIStorage.update(role.getRoleEntity());
        }
        getSceneBySceneId(sceneId).appearInScene(visibleSceneObject);
    }
}