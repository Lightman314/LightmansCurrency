package io.github.lightman314.lightmanscurrency.features.wallet;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public final class WalletEventListener {
    private WalletEventListener() {}

    public static boolean canPlayerPickup(ItemEntity item,Entity walletHolder) {
        if(item.hasPickUpDelay())
            return false;
        return item.getTarget() == null || item.getTarget().equals(walletHolder.getUUID());
    }

}