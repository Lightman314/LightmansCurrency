package io.github.lightman314.lightmanscurrency.api.upgrades;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.UpgradeNode;
import io.github.lightman314.lightmanscurrency.api.upgrades.event.UpgradeEvent;
import io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage;
import net.minecraft.core.component.DataComponentGetter;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Interface applied to all upgradeable machines, used to help determine if the machine can utilize the upgrade.<br>
 * <b>NOTE:</b> For Traders, this interface is applied to the {@link UpgradeNode Upgrade Node}, not directly on the TraderData.
 */
public interface IUpgradeable {

    UpgradeStorage getStorage();
    boolean allowUpgrade(UpgradeType type);

    /**
     * Checks both the {@link #allowUpgrade(UpgradeType)} method, as well as posting the {@link UpgradeEvent.AllowUpgradeEvent} event to determine if the upgrade is allowed in storage
     * @param host The Upgradeable object that the upgrade is attempting to be inserted into
     * @param type The upgrade type
     * @param itemState The data component getter for the upgrade item stack/resource
     * @return Whether the upgrade item can be inserted into the machines upgrade storage.<br>
     * Will return {@code false} without posting the vent if the new upgrade is unique and already present.
     */
    static boolean isUpgradeAllowed(IUpgradeable host,UpgradeType type, DataComponentGetter itemState) {
        //Hard-code the unique upgrade conflict
        if(type.isUnique() && host.getStorage().hasUpgrade(type))
            return false;
        //Otherwise rely on the event
        UpgradeEvent.AllowUpgradeEvent event = new UpgradeEvent.AllowUpgradeEvent(host,type,itemState);
        NeoForge.EVENT_BUS.post(event);
        return event.isAllowed();
    }

}
