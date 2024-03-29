package io.github.lightman314.lightmanscurrency.common.menus.containers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class SuppliedItemContainer implements Container {

    private final Supplier<IItemInteractable> supplier;

    public SuppliedItemContainer(@Nonnull Supplier<IItemInteractable> supplier) { this.supplier = supplier; }

    @Override
    public int getContainerSize() { return 1; }

    @Override
    public boolean isEmpty() { return this.getItem(0).isEmpty(); }

    @Nonnull
    @Override
    public ItemStack getItem(int slot) {
        if(slot != 0)
            return ItemStack.EMPTY;
        IItemInteractable interactable = this.supplier.get();
        if(interactable == null)
            return ItemStack.EMPTY;
        return interactable.getItem();
    }

    @Nonnull
    @Override
    public ItemStack removeItem(int slot, int count) {
        if(slot != 0)
            return ItemStack.EMPTY;
        IItemInteractable interactable = this.supplier.get();
        if(interactable == null)
            return ItemStack.EMPTY;
        return interactable.getItem().split(count);
    }

    @Nonnull
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if(slot != 0)
            return ItemStack.EMPTY;
        IItemInteractable interactable = this.supplier.get();
        if(interactable == null)
            return ItemStack.EMPTY;
        ItemStack result = interactable.getItem();
        interactable.setItem(ItemStack.EMPTY);
        return result;
    }

    @Override
    public void setItem(int slot, @Nonnull ItemStack stack) {
        if(slot != 0)
            return;
        IItemInteractable interactable = this.supplier.get();
        if(interactable == null)
            return;
        interactable.setItem(stack);
    }

    @Override
    public void setChanged() { }

    @Override
    public boolean stillValid(@Nonnull Player player) { return true; }

    @Override
    public void clearContent() { }

    public interface IItemInteractable
    {
        @Nonnull
        ItemStack getItem();
        void setItem(@Nonnull ItemStack item);
    }

}