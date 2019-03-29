package com.mmorpg.mbdl.business.world.scene.packet;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.mmorpg.mbdl.business.common.PacketIdManager;
import com.mmorpg.mbdl.business.world.scene.packet.vo.SceneCanGoInfo;
import com.mmorpg.mbdl.framework.communicate.websocket.annotation.ProtoDesc;
import com.mmorpg.mbdl.framework.communicate.websocket.model.AbstractPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * 场景信息响应包
 *
 * @author Sando Geek
 * @since v1.0 2018/12/26
 **/
@ProtoDesc(description = "场景信息响应包")
public class SceneUiInfoResp extends AbstractPacket {
    @Protobuf(description = "当前场景id",required = true)
    private Integer sceneId;
    @Protobuf(description = "当前场景名称",required = true)
    private String sceneName;
    @Protobuf(description = "可前往的场景列表")
    private List<SceneCanGoInfo> sceneCanGoInfos = new ArrayList<>();

    public SceneUiInfoResp setSceneId(Integer sceneId) {
        this.sceneId = sceneId;
        return this;
    }

    public SceneUiInfoResp setSceneName(String sceneName) {
        this.sceneName = sceneName;
        return this;
    }

    public List<SceneCanGoInfo> getSceneCanGoInfos() {
        return sceneCanGoInfos;
    }

    @Override
    public short getPacketId() {
        return PacketIdManager.SCENE_UI_INFO_RESP;
    }
}