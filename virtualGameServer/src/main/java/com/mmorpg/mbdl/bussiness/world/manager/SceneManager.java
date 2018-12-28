package com.mmorpg.mbdl.bussiness.world.manager;

import com.mmorpg.mbdl.bussiness.object.model.AbstractVisibleSceneObject;
import com.mmorpg.mbdl.bussiness.object.model.Role;
import com.mmorpg.mbdl.bussiness.role.entity.RoleEntity;
import com.mmorpg.mbdl.bussiness.world.resource.SceneRes;
import com.mmorpg.mbdl.bussiness.world.scene.model.Scene;
import com.mmorpg.mbdl.bussiness.world.scene.packet.SceneUiInfoResp;
import com.mmorpg.mbdl.bussiness.world.scene.packet.vo.SceneCanGoInfo;
import com.mmorpg.mbdl.framework.resource.exposed.IStaticRes;
import com.mmorpg.mbdl.framework.storage.core.IStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
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
    private IStaticRes<Integer, SceneRes> id2SceneRes;
    @Autowired
    private IStorage<Long, RoleEntity> roleEntityIStorage;
    @Autowired
    private SpawnManager spawnManager;

    @PostConstruct
    private void init() {
        self = this;
        // 初始化场景
        id2SceneRes.values().forEach(sceneRes -> {
            Scene scene = new Scene().setName(sceneRes.getName())
                    .setSceneId(sceneRes.getSceneId());
            sceneId2SceneMap.put(scene.getSceneId(),scene);
        });
        spawnManager.spawnAll(sceneId2SceneMap.values());

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
    public void switchToSceneById(AbstractVisibleSceneObject visibleSceneObject, int sceneId){
        getSceneBySceneId(visibleSceneObject.getSceneId()).disappearInScene(visibleSceneObject);
        visibleSceneObject.setSceneId(sceneId);
        if (visibleSceneObject instanceof Role){
            Role role = (Role) visibleSceneObject;
            role.getRoleEntity().setSceneId(sceneId);
            roleEntityIStorage.update(role.getRoleEntity());
        }
        getSceneBySceneId(sceneId).appearInScene(visibleSceneObject);
    }

    /**
     * 获取角色所在场景的前端需要的信息
     * @param role
     * @return
     */
    public SceneUiInfoResp getSceneUiInfoResp(Role role) {
        SceneUiInfoResp sceneUiInfoResp = new SceneUiInfoResp();
        int sceneId = role.getSceneId();
        sceneUiInfoResp.setSceneName(id2SceneRes.get(sceneId).getName());
        List<SceneCanGoInfo> sceneCanGoInfos = sceneUiInfoResp.getSceneCanGoInfos();
        List<Integer> canGoList = id2SceneRes.get(sceneId).getCanGoList();
        canGoList.forEach(integer -> sceneCanGoInfos.add(new SceneCanGoInfo()
                .setSceneName(id2SceneRes.get(integer).getName())
                .setSceneId(integer)
        ));
        return sceneUiInfoResp;
    }
}
