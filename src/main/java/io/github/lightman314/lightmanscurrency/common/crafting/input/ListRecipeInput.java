package io.github.lightman314.lightmanscurrency.common.crafting.input;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import javax.annotation.Nonnull;
import java.util.List;

public class ListRecipeInput implements RecipeInput {

    private final List<ItemStack> items;

    public ListRecipeInput(@Nonnull Container container) { this.items = ImmutableList.copyOf(InventoryUtil.buildList(container)); }
    public ListRecipeInput(@Nonnull List<ItemStack> items) { this.items = ImmutableList.copyOf(InventoryUtil.copyList(items)); }

    @Nonnull
    @Override
    public ItemStack getItem(int index) { return this.items.get(index); }

    @Override
    public int size() { return this.items.size(); }

}
