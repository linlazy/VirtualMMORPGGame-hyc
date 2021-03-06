package com.mmorpg.mbdl.framework.communicate.websocket.model;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.mmorpg.mbdl.framework.event.preset.SessionCloseEvent;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理器
 * @author sando
 */
@Component
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static SessionManager self;
    public static SessionManager getIntance(){
        return self;
    }
    @PostConstruct
    private void init(){
        self = this;
    }

    private ConcurrentHashMap<ChannelId, ISession> channelId2ISessions = new ConcurrentHashMap<>();

    /**
     * 添加ISession
     */
    public void add(ISession session){
        if (channelId2ISessions.containsKey(session.getId())){
            logger.error("session[channelId={},IP={}]重复注册",session.getId(),session.getIp());
        } else {
            channelId2ISessions.put(session.getId(),session);
        }
    }

    /**
     * 根据channelId移除相应的ISession
     */
    @Subscribe
    @AllowConcurrentEvents
    public void remove(SessionCloseEvent sessionCloseEvent){
        // logger.info("会话关闭事件触发成功！！！");
        channelId2ISessions.remove(sessionCloseEvent.getSession().getId());
    }

    /**
     * 根据channelId获取session
     * @param channelId
     * @return 对应的ISession
     */
    public ISession getSession(ChannelId channelId){
        return channelId2ISessions.get(channelId);
    }
}
