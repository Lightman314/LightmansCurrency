package io.github.lightman314.lightmanscurrency.client.colors;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class DyedAndInvertedColor implements ItemColor {

    public static final ItemColor INSTANCE = new DyedAndInvertedColor(0,1);

    private final int normalIndex;
    private final int invertedIndex;
    public DyedAndInvertedColor(int normalIndex,int invertedIndex) {
        this.normalIndex = normalIndex;
        this.invertedIndex = invertedIndex;
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        int color = DyedItemColor.getOrDefault(stack,0xFFFFFFFF);
        if(tintIndex == this.normalIndex)
            return color;
        if(tintIndex == this.invertedIndex)
            return FastColor.ARGB32.opaque(0xFFFFFFFF - color);
        return 0xFFFFFFFF;
    }

}
