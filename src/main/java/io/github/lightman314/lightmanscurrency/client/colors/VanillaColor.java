package io.github.lightman314.lightmanscurrency.client.colors;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class VanillaColor implements ItemColor {
    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
        if(tintIndex == 0 && itemStack.getItem() instanceof DyeableLeatherItem item)
            return item.getColor(itemStack);
        return 0xFFFFFF;
    }
}
