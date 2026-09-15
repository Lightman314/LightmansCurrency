package io.github.lightman314.lightmanscurrency.api.helpers.item_selection;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public record ItemSelectionFilter(Identifier key,Predicate<ItemStack> filter) implements Predicate<ItemStack> {
    @Override
    public boolean test(ItemStack stack) { return this.filter.test(stack); }
    @Override
    public int hashCode() { return this.key.hashCode(); }
}