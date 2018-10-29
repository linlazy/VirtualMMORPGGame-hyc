package com.mmorpg.mbdl.framework.communicate.websocket.model;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;

public interface ISession {
    /**
     * 获取Session的id（其实是ChannelId）
     * @return channel().id()
     */
    ChannelId getId();

    /**
     * 获取客户端ip地址
     * @return ip地址
     */
    String getIp();

    /**
     * 获取会话状态
     * @return 会话状态
     */
    SessionState getState();

    /**
     *设置会话状态
     */
    void setState(SessionState state);

    /**
     * 发送一个响应包
     * @param abstractPacket 抽象包
     * @return ChannelFuture
     */
    ChannelFuture sendPacket(AbstractPacket abstractPacket);

    // String getUid(); java.rmi.server.UID
    /**
     * 获取玩家id
     * @return 玩家id
     */
    Long getPlayerId();
}
