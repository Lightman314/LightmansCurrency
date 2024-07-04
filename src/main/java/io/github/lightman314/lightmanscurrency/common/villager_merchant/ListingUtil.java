package io.github.lightman314.lightmanscurrency.common.villager_merchant;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ListingUtil {

    @Nonnull
    public static ItemCost costFor(@Nonnull ItemStack stack)
    {
        if(stack.getComponents().isEmpty())
            return new ItemCost(stack.getItem(),stack.getCount());
        else
        {
            Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem());
            int count = stack.getCount();
            return new ItemCost(holder,count, DataComponentPredicate.allOf(stack.getComponents()));
        }
    }

    @Nonnull
    public static Optional<ItemCost> optionalCost(@Nonnull ItemStack stack)
    {
        if(stack.isEmpty())
            return Optional.empty();
        else
            return Optional.of(costFor(stack));
    }

}
