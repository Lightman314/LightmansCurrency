package io.github.lightman314.lightmanscurrency.features.trader.misc;

import net.minecraft.world.item.ItemStack;

public interface IStorageFilterNode {

    boolean itemAllowedInStorage(ItemStack stack);

}