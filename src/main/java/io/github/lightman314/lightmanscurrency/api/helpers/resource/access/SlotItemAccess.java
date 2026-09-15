package io.github.lightman314.lightmanscurrency.api.helpers.resource.access;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SlotItemAccess extends SnapshotJournal<ItemStack> implements ItemAccess  {

    public final Slot slot;
    private SlotItemAccess(Slot slot) { this.slot = slot; }

    public static SlotItemAccess of(Slot slot) { return new SlotItemAccess(slot); }

    @Override
    public ItemResource getResource() { return ItemResource.of(this.slot.getItem()); }

    @Override
    public int getAmount() { return this.slot.getItem().getCount(); }

    @Override
    public int insert(ItemResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        ItemStack currentStack = this.slot.getItem();
        if(currentStack.isEmpty() || resource.matches(currentStack)) {
            int insertAmount = Math.min(amount,this.slot.getMaxStackSize(resource.toStack()) - currentStack.getCount());
            if(insertAmount > 0) {
                this.updateSnapshots(transaction);
                if(currentStack.isEmpty())
                    this.slot.set(resource.toStack(insertAmount));
                else {
                    currentStack.grow(insertAmount);
                    this.slot.setChanged();
                }
                return insertAmount;
            }
        }
        return 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        ItemStack currentStack = this.slot.getItem();
        if(currentStack.isEmpty() || !resource.matches(currentStack))
            return 0;
        int removeAmount = Math.min(amount,currentStack.getCount());
        if(removeAmount > 0) {
            this.updateSnapshots(transaction);
            currentStack.shrink(removeAmount);
            if(currentStack.isEmpty())
                this.slot.set(ItemStack.EMPTY);
            else
                this.slot.setChanged();
        }
        return removeAmount;
    }

    @Override
    protected ItemStack createSnapshot() { return this.slot.getItem().copy(); }

    @Override
    protected void revertToSnapshot(ItemStack snapshot) { this.slot.set(snapshot); }

}
