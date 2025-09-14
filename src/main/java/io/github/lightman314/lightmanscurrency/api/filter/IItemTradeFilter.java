package io.github.lightman314.lightmanscurrency.api.filter;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IItemTradeFilter {

    @Nullable
    Predicate<ItemStack> getFilter(ItemStack stack);
    @Nullable
    List<Component> getCustomTooltip(ItemStack stack);
    List<ItemStack> getDisplayableItems(ItemStack stack, @Nullable IItemHandler availableItems);

}
