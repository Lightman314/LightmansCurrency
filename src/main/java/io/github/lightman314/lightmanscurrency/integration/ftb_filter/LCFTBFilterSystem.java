package io.github.lightman314.lightmanscurrency.integration.ftb_filter;

import dev.ftb.mods.ftbfiltersystem.api.FTBFilterSystemAPI;
import dev.ftb.mods.ftbfiltersystem.api.FilterException;
import io.github.lightman314.lightmanscurrency.api.filter.FilterAPI;
import io.github.lightman314.lightmanscurrency.integration.ftb_filter.filter.FTBFilter;
import net.minecraft.world.item.ItemStack;

public class LCFTBFilterSystem {

    public static void setup()
    {
        //Run dummy code to confirm the mod is loaded correctly
        FTBFilterSystemAPI.rl("test");
        FilterAPI.registerFilterProvider(item -> {
            if(hasFilter(item))
                return FTBFilter.INSTANCE;
            return null;
        });
    }

    public static boolean hasFilter(ItemStack stack)
    {
        if(FTBFilterSystemAPI.api().isFilterItem(stack))
        {
            try {
                FTBFilterSystemAPI.api().parseFilter(stack);
                return true;
            }catch (FilterException ignored) {}
        }
        return false;
    }

}