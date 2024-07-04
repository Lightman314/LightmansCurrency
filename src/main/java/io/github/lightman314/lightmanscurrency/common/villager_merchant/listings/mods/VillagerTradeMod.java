package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class VillagerTradeMod {


    @Nonnull
    public abstract ItemCost modifyCost(@Nullable Entity villager, @Nonnull ItemCost cost);

    @Nonnull
    public abstract ItemStack modifyResult(@Nullable Entity villager, @Nonnull ItemStack result);

    protected final ItemCost copyWithNewItem(@Nonnull ItemCost cost, @Nullable Item replacement)
    {
        if(replacement == null)
            return cost;
        return new ItemCost(BuiltInRegistries.ITEM.createIntrusiveHolder(replacement),cost.count(),cost.components());
    }
    protected final ItemStack copyWithNewItem(@Nonnull ItemStack result, @Nullable Item replacement)
    {
        if(replacement == null)
            return result;
        return result.transmuteCopy(replacement,result.getCount());
    }

}
