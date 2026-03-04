package io.github.lightman314.lightmanscurrency.client.colors;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class VanillaColor implements ItemColor {

    public static final ItemColor INSTANCE = new VanillaColor(0);

    private final int tintIndex;
    public VanillaColor(int tintIndex) { this.tintIndex = tintIndex; }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if(tintIndex == this.tintIndex)
            return DyedItemColor.getOrDefault(stack,-6265536);
        return 0xFFFFFFFF;
    }
}
