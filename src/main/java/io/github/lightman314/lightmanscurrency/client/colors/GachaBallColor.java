package io.github.lightman314.lightmanscurrency.client.colors;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GachaBallColor implements ItemColor {
    @Override
    public int getColor(ItemStack stack, int layer) {
        if(layer == 0)
        {
            CompoundTag tag = stack.getTagElement("display");
            return tag != null && tag.contains("color",Tag.TAG_ANY_NUMERIC) ? tag.getInt("color") : 0xFFFFFF;
        }
        return -1;
    }
}
