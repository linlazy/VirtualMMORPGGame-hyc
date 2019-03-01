package com.mmorpg.mbdl.business.container.model.handler;

import com.mmorpg.mbdl.business.container.model.AbstractItem;
import com.mmorpg.mbdl.business.container.model.Container;
import com.mmorpg.mbdl.business.container.model.ItemType;
import com.mmorpg.mbdl.business.container.res.ItemRes;
import com.mmorpg.mbdl.business.role.model.Role;
import com.mmorpg.mbdl.business.role.model.prop.PropType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 * 抽象物品使用处理器
 *
 * @author Sando Geek
 * @since v1.0 2019/2/15
 **/
public abstract class AbstractItemUseHandler {
    /**
     * 需要与前端同步的属性类型
      */
    // protected static Set<PropType> propTypesNeedSync = new HashSet<>();
    // static {
    //     propTypesNeedSync.add(PropType.CURRENT_HP);
    //     propTypesNeedSync.add(PropType.CURRENT_MP);
    //     propTypesNeedSync.add(PropType.MAX_HP);
    //     propTypesNeedSync.add(PropType.MAX_MP);
    // }
    @Autowired
    private ItemUseHandlerManager itemUseHandlerManager;

    @PostConstruct
    public void register() {
        itemUseHandlerManager.register(this);
    }

    /**
     * 获取使用的物品的类型
     * @return
     */
    public abstract ItemType getItemType();

    protected void applyPropChange(Role role,Map<PropType,Long> propTypeLongMap) {
        // boolean customUiInfoChange = false;
        Set<Map.Entry<PropType, Long>> entries = propTypeLongMap.entrySet();
        for (Map.Entry<PropType, Long> entry :
                entries) {
            // if (!customUiInfoChange && propTypesNeedSync.contains(entry.getKey())) {
            //     customUiInfoChange = true;
            // }
            role.getPropManager().getPropTreeByType(entry.getKey()).addRootNodeValue(entry.getValue());
        }
        // if (customUiInfoChange) {
        //     role.sendPacket(role.getCustomRoleUiInfoResp());
        // }
    }

    /**
     * 使用最大堆叠数为1的物品时调用（此时请求传入的是objectId）
     * @param role 使用物品的角色
     * @param packContainer
     * @param abstractItem
     * @param itemRes
     * @param objectId
     * @return 使用成功返回true,否则返回false
     */
    public abstract boolean useById(Role role, Container packContainer, AbstractItem abstractItem, ItemRes itemRes, long objectId);

    /**
     * 使用最大堆叠数不为1的物品时调用（此时请求传入的是key）
     */
    public abstract boolean useByKey(Role role, Container packContainer, int key, int amount, ItemRes itemRes);
}
