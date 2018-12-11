package com.mmorpg.mbdl.bussiness.login.service;

import com.mmorpg.mbdl.bussiness.login.model.LoginResultType;
import com.mmorpg.mbdl.bussiness.login.packet.LoginAuthReq;
import com.mmorpg.mbdl.bussiness.login.packet.LoginResultResp;
import com.mmorpg.mbdl.bussiness.register.entity.PlayerAccountEntity;
import com.mmorpg.mbdl.framework.communicate.websocket.model.ISession;
import com.mmorpg.mbdl.framework.communicate.websocket.model.SessionState;
import com.mmorpg.mbdl.framework.storage.core.IStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 登陆服务
 *
 * @author Sando Geek
 * @since v1.0 2018/12/10
 **/
@Component
public class LoginService {
    @Autowired
    private IStorage<String, PlayerAccountEntity> playerAccountEntityIStorage;

    public LoginResultResp login(ISession session, LoginAuthReq loginAuthReq){
        // TODO 密码采用非对称加密传输并存储到数据库
        PlayerAccountEntity playerAccountEntity = playerAccountEntityIStorage.get(loginAuthReq.getAccount());
        LoginResultResp loginResultResp = new LoginResultResp();
        if (playerAccountEntity == null) {
            loginResultResp.setResultType(LoginResultType.FAILURE);
        } else if (loginAuthReq.getPassword().equals(playerAccountEntity.getPassword())){
            loginResultResp.setResultType(LoginResultType.SUCESS);
            session.setState(SessionState.LOGINED);
        }
        // String message = String.format("协议[%s-%s]分发成功：帐号：%s,密码：%s",req.getPacketId(),req.getClass().getSimpleName(),req.getAccount(),req.getPassword());
        return loginResultResp;
    }
}
