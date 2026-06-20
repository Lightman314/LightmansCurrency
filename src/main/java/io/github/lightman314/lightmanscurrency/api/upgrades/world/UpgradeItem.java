package io.github.lightman314.lightmanscurrency.api.upgrades.world;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.world.item.Item;

public class UpgradeItem extends Item {

    public UpgradeItem(Properties properties,UpgradeType upgradeType) {
        super(properties.component(LCDataComponents.UPGRADE_TYPE.get(),upgradeType));
    }

}