package io.github.lightman314.lightmanscurrency.api.filter;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IItemTradeFilter {

    @Nullable
    Predicate<ItemStack> getFilter(ItemStack stack);
    @Nullable
    List<Component> getCustomTooltip(ItemStack stack);
    default List<ItemStack> getDisplayableItems(ItemStack stack, @Nullable IItemHandler availableItems)
    {
        List<ItemStack> results = new ArrayList<>();
        Predicate<ItemStack> filter = this.getFilter(stack);
        if(filter == null)
            return results;
        if(availableItems != null)
        {
            for(int i = 0; i < availableItems.getSlots(); ++i)
            {
                ItemStack item = availableItems.getStackInSlot(i);
                if(!item.isEmpty() && filter.test(item) && !isInList(results,item))
                    results.add(item.copyWithCount(stack.getCount()));
            }
        }
        else
        {
            try {
                //Try rebuild creative mode tabs
                boolean hasPermissions = LightmansCurrency.getProxy().getHasPermissionsSetting();
                Level level = LightmansCurrency.getProxy().safeGetDummyLevel();
                if(level != null)
                    CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(),hasPermissions,level.registryAccess());
            } catch (Exception e) {
                LightmansCurrency.LogWarning("Failed to rebuild creative tabs, display items may be inaccurate.",e);
            }
            for(CreativeModeTab tab : CreativeModeTabs.allTabs())
            {
                if(tab.getType() != CreativeModeTab.Type.CATEGORY)
                    continue;
                for(ItemStack item : tab.getDisplayItems())
                {
                    if(filter.test(item) && !isInList(results,item))
                        results.add(item.copyWithCount(stack.getCount()));
                }
            }
        }
        return results;
    }

    static boolean isInList(List<ItemStack> list, ItemStack item)
    {
        for(ItemStack i : list)
        {
            if(InventoryUtil.ItemMatches(i,item))
                return true;
        }
        return false;
    }

}