package com.mmorpg.mbdl.business.equip.model.handler;

import com.mmorpg.mbdl.business.equip.model.Equip;
import com.mmorpg.mbdl.business.equip.model.EquipType;
import com.mmorpg.mbdl.business.role.model.Role;
import org.springframework.stereotype.Component;

/**
 * 武器处理器
 *
 * @author Sando Geek
 * @since v1.0 2019/2/18
 **/
@Component
public class WeaponHandler extends AbstractEquipHandler {

    @Override
    public EquipType getEquipType() {
        return EquipType.WEAPON;
    }

    @Override
    public Equip equip(Role role, Equip toEquip) {
        Equip oldWeapon = role.getEquipEntity().getWeapon();
        role.getEquipEntity().setArmor(toEquip);
        itemReses.get(toEquip.getKey()).getPropChangeAfterUse().forEach((propType, aLong) -> {
            role.getPropManager().getOrCreateTree(propType).getOrCreateChild("equip").getOrCreateChild("武器").set(aLong);
        });
        return oldWeapon;
    }
}
