package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public interface ITradeSearchFilter {

    void filterTrade(TradeData data, PendingSearch search, HolderLookup.Provider lookup);

    static boolean filterItem(ItemStack stack, String searchText, HolderLookup.Provider lookup)
    {
        if(!stack.isEmpty())
        {
            //Check Item Name
            if(stack.getHoverName().getString().toLowerCase().contains(searchText))
                return true;
            //Check Item ID
            if(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase().contains(searchText))
                return true;
            //Check Item Enchantments
            ItemEnchantments enchantments = stack.getItem() == Items.ENCHANTED_BOOK ? stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS,ItemEnchantments.EMPTY) : stack.getAllEnchantments(lookup.lookupOrThrow(Registries.ENCHANTMENT));
            for(var ench : enchantments.entrySet())
            {
                if(Enchantment.getFullname(ench.getKey(),ench.getIntValue()).getString().toLowerCase().contains(searchText))
                    return true;
                if(ench.getKey().getRegisteredName().toLowerCase().contains(searchText))
                    return true;
            }
        }
        return false;
    }

    static boolean filterItemTooltips(ItemStack stack, String searchText, HolderLookup.Provider lookup)
    {
        if(!stack.isEmpty())
        {
            List<Component> tooltip = stack.getTooltipLines(Item.TooltipContext.of(lookup),null,TooltipFlag.NORMAL);
            for(Component line : tooltip)
            {
                if(line.getString().toLowerCase().contains(searchText))
                    return true;
            }
        }
        return false;
    }

}
