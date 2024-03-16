package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class VillagerTradeMod {


    @Nonnull
    public abstract ItemStack modifyCost(@Nullable Entity villager, @Nonnull ItemStack cost);

    @Nonnull
    public abstract ItemStack modifyResult(@Nullable Entity villager, @Nonnull ItemStack result);

    protected final ItemStack copyWithNewItem(@Nonnull ItemStack stack, @Nullable Item replacement)
    {
        if(replacement == null)
            return stack;
        ItemStack copy = new ItemStack(replacement);
        copy.setCount(stack.getCount());
        if(stack.hasTag())
            copy.setTag(stack.getTag().copy());
        return copy;
    }

}
