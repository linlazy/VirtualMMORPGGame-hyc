package com.mmorpg.mbdl.bussiness.register.service;

import com.mmorpg.mbdl.bussiness.register.entity.PlayerAccountEntity;
import com.mmorpg.mbdl.bussiness.register.packet.RegisterReq;
import com.mmorpg.mbdl.bussiness.register.packet.RegisterResp;
import com.mmorpg.mbdl.framework.common.generator.IdGeneratorFactory;
import com.mmorpg.mbdl.framework.storage.core.IStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 注册服务
 *
 * @author Sando Geek
 * @since v1.0 2018/12/7
 **/
@Component
public class RegisterService {
    @Autowired
    private IStorage<String, PlayerAccountEntity> playerAccountEntityIStorage;
    @Autowired
    private IdGeneratorFactory idGeneratorFactory;

    public RegisterResp register(RegisterReq registerReq){
        RegisterResp registerResp = new RegisterResp();
       if (playerAccountEntityIStorage.get(registerReq.getAccount())!=null){
           registerResp.setSuccess(false);
       }else {
           playerAccountEntityIStorage.create(registerReq.getAccount(),(id)->{
               PlayerAccountEntity playerAccountEntity = new PlayerAccountEntity();
               playerAccountEntity.setAccount(id);
               playerAccountEntity.setPassword(registerReq.getPassword());
               return playerAccountEntity;
           });
           registerResp.setSuccess(true);
       }
       return registerResp;
    }
}
