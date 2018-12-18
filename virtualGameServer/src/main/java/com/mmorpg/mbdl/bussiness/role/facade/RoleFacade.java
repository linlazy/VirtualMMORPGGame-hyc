package com.mmorpg.mbdl.bussiness.role.facade;

import com.mmorpg.mbdl.bussiness.role.packet.AddRoleReq;
import com.mmorpg.mbdl.bussiness.role.packet.AddRoleResp;
import com.mmorpg.mbdl.bussiness.role.packet.GetRoleListReq;
import com.mmorpg.mbdl.bussiness.role.packet.GetRoleListResp;
import com.mmorpg.mbdl.bussiness.role.service.RoleService;
import com.mmorpg.mbdl.framework.communicate.websocket.annotation.PacketHandler;
import com.mmorpg.mbdl.framework.communicate.websocket.annotation.PacketMethod;
import com.mmorpg.mbdl.framework.communicate.websocket.model.ISession;
import com.mmorpg.mbdl.framework.communicate.websocket.model.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 角色门面
 *
 * @author Sando Geek
 * @since v1.0 2018/12/17
 **/
@PacketHandler
public class RoleFacade {
    private static Logger logger = LoggerFactory.getLogger(RoleFacade.class);

    @Autowired
    private RoleService roleService;

    @PacketMethod(state = SessionState.LOGINED)
    public AddRoleResp handleAddRoleReq(ISession session, AddRoleReq addRoleReq) {
        return roleService.handleAddRoleReq(session,addRoleReq);
    }
    @PacketMethod(state = SessionState.LOGINED)
    public GetRoleListResp handleGetRoleListReq(ISession session, GetRoleListReq getRoleListReq) {
        return roleService.handleGetRoleListReq(session,getRoleListReq);
    }
}
