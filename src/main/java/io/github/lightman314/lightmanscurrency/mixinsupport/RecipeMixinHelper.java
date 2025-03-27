package io.github.lightman314.lightmanscurrency.mixinsupport;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class RecipeMixinHelper {

    public static boolean shouldBlockRecipe(Container input) {
        for(int i = 0; i < input.getContainerSize(); ++i)
        {
            ItemStack item = input.getItem(i);
            if(!item.isEmpty())
            {
                CompoundTag itemTag = item.getTag();
                if(itemTag != null)
                    return itemTag.contains("StoredTrader", CompoundTag.TAG_LONG);
            }
        }
        return false;
    }

}