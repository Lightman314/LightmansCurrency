package io.github.lightman314.lightmanscurrency.common.items.colored;

import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public interface ColoredItem extends DyeableLeatherItem {

    @Override
    default int getColor(@Nonnull ItemStack stack) {
        if(this.hasCustomColor(stack))
            return DyeableLeatherItem.super.getColor(stack);
        return 0xFFFFFF;
    }

    static void setItemColor(@Nonnull ItemStack stack, int color)
    {
        if(stack.getItem() instanceof DyeableLeatherItem item)
            item.setColor(stack,color);
    }

}
