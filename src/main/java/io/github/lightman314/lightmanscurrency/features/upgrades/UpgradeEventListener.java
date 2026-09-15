package io.github.lightman314.lightmanscurrency.features.upgrades;

import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.event.UpgradeEvent;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCUpgrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public final class UpgradeEventListener {
    private UpgradeEventListener() {}

    @SubscribeEvent
    private static void addDefaultUpgradeTargets(UpgradeEvent.CollectUpgradeTargetsEvent event) {
        if(event.isUpgrade(LCUpgrades.ITEM_CAPACITY))
        {
            event.addTarget(LCText.Upgrades.TOOLTIP_UPGRADE_TARGET_TRADER_ITEM);
        }
    }

}
