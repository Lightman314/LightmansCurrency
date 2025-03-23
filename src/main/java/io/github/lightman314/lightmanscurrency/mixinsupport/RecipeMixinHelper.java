package io.github.lightman314.lightmanscurrency.mixinsupport;

import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class RecipeMixinHelper {

    public static boolean shouldBlockRecipe(RecipeInput input) {
        for(int i = 0; i < input.size(); ++i)
        {
            ItemStack item = input.getItem(i);
            if(!item.isEmpty() && item.has(ModDataComponents.TRADER_ITEM_DATA))
                return true;
        }
        return false;
    }

}
