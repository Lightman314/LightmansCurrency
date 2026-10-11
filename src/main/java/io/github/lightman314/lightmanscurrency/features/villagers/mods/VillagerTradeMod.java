package io.github.lightman314.lightmanscurrency.features.villagers.mods;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;

import javax.annotation.Nullable;

public abstract class VillagerTradeMod {

    public abstract ItemCost modifyCost(@Nullable Entity villager,ItemCost cost);
    public abstract ItemStack modifyResult(@Nullable Entity villager,ItemStack result);

    @SuppressWarnings("deprecation")
    protected final ItemCost copyWithNewItem(ItemCost cost,@Nullable Item replacement) {
        if(replacement == null)
            return cost;
        return new ItemCost(replacement.builtInRegistryHolder(),cost.count(),cost.components());
    }
    protected final ItemStack copyWithNewItem(ItemStack result,@Nullable Item replacement) {
        if(replacement == null)
            return result;
        return result.transmuteCopy(replacement);
    }

}