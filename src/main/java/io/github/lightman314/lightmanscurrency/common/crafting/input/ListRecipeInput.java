package io.github.lightman314.lightmanscurrency.common.crafting.input;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.List;

public class ListRecipeInput implements RecipeInput {

    private final List<ItemStack> items;

    public ListRecipeInput(Container container) { this.items = ImmutableList.copyOf(ItemHandlerUtil.toList(new InvWrapper(container))); }
    public ListRecipeInput(List<ItemStack> items) { this.items = ImmutableList.copyOf(ItemHandlerUtil.copyList(items)); }
    public ListRecipeInput(IItemHandler container) {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builderWithExpectedSize(container.getSlots());
        for(int i = 0; i < container.getSlots(); ++i)
            builder.add(container.getStackInSlot(i));
        this.items = builder.build();
    }

    @Override
    public ItemStack getItem(int index) { return this.items.get(index); }

    @Override
    public int size() { return this.items.size(); }

}
