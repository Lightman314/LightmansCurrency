package io.github.lightman314.lightmanscurrency.api.helpers;

import io.github.lightman314.lightmanscurrency.client.LCClientRecipeCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class RecipeHelper {

    private RecipeHelper() {}

    public static RecipeMap getRecipeMap(Level level) {
        if(level.isClientSide())
            return LCClientRecipeCache.getRecipeMap();
        else if(level instanceof ServerLevel sl)
            return sl.getServer().getRecipeManager().recipeMap();
        return RecipeMap.EMPTY;
    }

    public static SlotDisplay asSlotDisplay(List<ItemStack> itemList) {
        List<SlotDisplay> displays = new ArrayList<>();
        for(ItemStack item : itemList) {
            //Empty Items become and empty slot display
            if(item.isEmpty())
                displays.add(SlotDisplay.Empty.INSTANCE);
            else
                displays.add(new SlotDisplay.ItemStackSlotDisplay(ItemHelper.asTemplate(item)));
        }
        return new SlotDisplay.Composite(displays);
    }

}
