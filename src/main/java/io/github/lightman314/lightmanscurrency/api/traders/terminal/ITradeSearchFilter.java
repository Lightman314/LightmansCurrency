package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public interface ITradeSearchFilter {

    void filterTrade(TradeData data, PendingSearch search);

    static boolean filterItem(ItemStack stack, String searchText)
    {
        if(!stack.isEmpty())
        {
            //Check Item Name
            if(stack.getHoverName().getString().toLowerCase().contains(searchText))
                return true;
            //Check Item ID
            if(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().toLowerCase().contains(searchText))
                return true;
            //Check Item Enchantments
            Map<Enchantment,Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
            for(var ench : enchantments.entrySet())
            {
                if(ench.getKey().getFullname(ench.getValue()).getString().toLowerCase().contains(searchText))
                    return true;
                if(ForgeRegistries.ENCHANTMENTS.getKey(ench.getKey()).toString().toLowerCase().contains(searchText))
                    return true;
            }
        }
        return false;
    }

}