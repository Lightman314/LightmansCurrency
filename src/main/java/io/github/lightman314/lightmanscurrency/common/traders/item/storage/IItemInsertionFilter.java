package io.github.lightman314.lightmanscurrency.common.traders.item.storage;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IItemInsertionFilter {
    boolean isItemRelevant(ItemStack stack);
}
