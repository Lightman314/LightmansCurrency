package io.github.lightman314.lightmanscurrency.common.traders.item.storage;

import net.minecraft.world.item.ItemStack;

public interface IItemExtractionFilter {
    boolean allowExtraction(ItemStack stack);
}