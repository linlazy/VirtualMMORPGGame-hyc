package com.mmorpg.mbdl.bussiness.role.service;

import com.mmorpg.mbdl.bussiness.role.dao.RoleEntityDao;
import com.mmorpg.mbdl.bussiness.role.entity.RoleEntity;
import com.mmorpg.mbdl.bussiness.role.packet.GetRoleListReq;
import com.mmorpg.mbdl.bussiness.role.packet.GetRoleListResp;
import com.mmorpg.mbdl.bussiness.role.packet.vo.RoleInfo;
import com.mmorpg.mbdl.framework.communicate.websocket.model.ISession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色服务
 *
 * @author Sando Geek
 * @since v1.0 2018/12/17
 **/
@Component
public class RoleService {
    @Autowired
    private RoleEntityDao roleEntityDao;
    public GetRoleListResp handleGetRoleListReq(ISession session, GetRoleListReq getRoleListReq) {
        List<RoleEntity> roleEntities = roleEntityDao.findAllByAccount(session.getAccount());
        GetRoleListResp roleListResp = new GetRoleListResp();
        List<RoleInfo> roleInfoList = roleListResp.getRoleInfoList();
        roleEntities.stream().forEach(roleEntity -> {
            RoleInfo roleInfo = new RoleInfo();
            roleInfo.setName(roleEntity.getName())
                    .setLevel(roleEntity.getLevel())
                    .setRoleType(roleEntity.getRoleType());
            roleInfoList.add(roleInfo);
        });
        return roleListResp;
    }
}
