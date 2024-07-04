package io.github.lightman314.lightmanscurrency.common.menus.containers;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.RecipeInput;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class RecipeContainerWrapper extends SuppliedContainer implements RecipeInput {

    public RecipeContainerWrapper(int size) { super(() -> new SimpleContainer(size)); }
    public RecipeContainerWrapper(@Nonnull Supplier<Container> supplier) { super(supplier); }
    public RecipeContainerWrapper(@Nonnull Supplier<Container> supplier, @Nonnull Container nullContainer) { super(supplier, nullContainer); }


    @Override
    public int size() { return this.getContainerSize(); }
}
