package io.github.lightman314.lightmanscurrency.api.misc.item_handlers;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.function.Supplier;

public class SuppliedInventory implements IItemHandlerModifiable {

    private final Supplier<IItemHandlerModifiable> source;
    public SuppliedInventory(Supplier<IItemHandlerModifiable> source) { this.source = source; }

    private IItemHandlerModifiable safeGet() {
        IItemHandlerModifiable result = this.source.get();
        if(result == null)
            return new ItemStackHandler(0);
        return result;
    }
    @Override
    public int getSlots() { return this.safeGet().getSlots(); }
    @Override
    public ItemStack getStackInSlot(int slot) { return this.safeGet().getStackInSlot(slot); }
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return this.safeGet().insertItem(slot,stack,simulate); }
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) { return this.safeGet().extractItem(slot,amount,simulate); }
    @Override
    public int getSlotLimit(int slot) { return this.safeGet().getSlotLimit(slot); }
    @Override
    public boolean isItemValid(int slot, ItemStack stack) { return this.safeGet().isItemValid(slot,stack); }
    @Override
    public void setStackInSlot(int slot, ItemStack stack) { this.safeGet().setStackInSlot(slot,stack); }
}
