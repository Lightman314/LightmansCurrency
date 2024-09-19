package io.github.lightman314.lightmanscurrency.client.colors;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import javax.annotation.Nonnull;

public class ATMCardColor implements ItemColor {
    @Override
    public int getColor(@Nonnull ItemStack stack, int layer) {
        int color = stack.getOrDefault(DataComponents.DYED_COLOR,new DyedItemColor(0xFFFFFF,true)).rgb();
        if(layer == 0)
            return 0xFF000000 + color;
        if(layer == 1)
            return 0xFFFFFFFF - color;

        return 0xFFFFFFFF;
    }
}
