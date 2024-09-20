package io.github.lightman314.lightmanscurrency.client.colors;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ATMCardColor implements ItemColor {

    @Override
    public int getColor(@Nonnull ItemStack stack, int layer) {
        int color = 0xFFFFFF;
        CompoundTag tag = stack.getTag();
        if(tag != null && tag.contains(ItemStack.TAG_DISPLAY))
        {
            CompoundTag display = tag.getCompound(ItemStack.TAG_DISPLAY);
            if(display.contains(ItemStack.TAG_COLOR))
                color = display.getInt(ItemStack.TAG_COLOR);
        }
        if(layer == 0)
            return color;
        if(layer == 1)
            return 0xFFFFFF - color;
        return 0xFFFFFF;
    }
}
