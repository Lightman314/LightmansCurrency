package io.github.lightman314.lightmanscurrency.client.colors;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GachaBallColor implements ItemColor {
    @Override
    public int getColor(ItemStack stack, int layer) {
        if(layer == 0)
            return 0xFF000000 + stack.getOrDefault(DataComponents.DYED_COLOR,new DyedItemColor(0xFFFFFF,false)).rgb();
        return 0;
    }
}
