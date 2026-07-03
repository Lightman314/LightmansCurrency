package io.github.lightman314.lightmanscurrency.api.helpers.resource;

import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A form of {@link ListBackedItemStorage list-backed item storage} with a predetermined number of slots<br>
 * May be extended for custom implementations that limit what items are allowed, or how many items can fit in each slot
 */
public class NormalItemStorage extends ListBackedItemStorage {

    public NormalItemStorage(int size) { super(NonNullList.withSize(size,ItemStack.EMPTY)); }

    /**
     * Changes the size of this container.<br>
     * Maintains any items already loaded from the existing slots, but any items in the extra slots will be voided
     * @param size The new size of this container
     */
    public final void overrideSize(int size)
    {
        int newSize = Math.max(size,1);
        if(newSize == this.storage.size())
            return;
        List<ItemStack> removed = new ArrayList<>();
        while(this.storage.size() > newSize)
        {
            ItemStack r = this.storage.removeLast();
            if(!r.isEmpty())
                removed.add(r);
        }
        while(this.storage.size() < newSize)
            this.storage.add(ItemStack.EMPTY);
        //Attempt to forcibly fit the wallets contents within its storage
        this.forceInsert(removed);
    }
    @Override
    public void copyFrom(List<ItemStack> list) {
        //Since it's a list with size, it'll keep its current size
        this.storage.clear();
        for(int i = 0; i < this.storage.size() && i < list.size(); ++i)
            this.storage.set(i,list.get(i));
    }

    protected final void forceInsert(List<ItemStack> list) {
        for(ItemStack item : ItemHelper.combineStacks(list))
            this.forceInsert(item);
    }

    protected final void forceInsert(ItemStack stack) {
        if(stack.isEmpty())
            return;
        for(int i = 0; i < this.storage.size() && !stack.isEmpty(); ++i)
        {
            ItemStack s = this.storage.get(i);
            if(s.isEmpty())
            {
                int insert = Math.min(Math.min(stack.getMaxStackSize(),stack.getCount()),this.getCapacityAsInt(i,ItemResource.of(stack)));
                if(insert > 0)
                    this.storage.set(i,stack.split(insert));
            }
            else if(ItemStack.isSameItemSameComponents(s,stack))
            {
                int insert = Math.min(s.getMaxStackSize() - s.getCount(),stack.getCount());
                if(insert > 0)
                {
                    s.grow(insert);
                    stack.shrink(insert);
                }
            }
        }
    }

    public final void setItem(int slot,ItemResource resource,int amount) { this.setItem(slot,resource.toStack(amount)); }
    public final void setItem(int slot,ItemStack stack)
    {
        if(slot < 0 || slot >= this.storage.size())
            return;
        List<ItemStack> oldData = ItemHelper.copyList(this.storage);
        this.storage.set(slot,stack);
        this.setChanged(oldData);
    }

    //Default to the items max stack size
    @Override
    public long getCapacityAsLong(int index, ItemResource resource) { return 64; }
    @Override
    public boolean isValid(int index, ItemResource resource) { return this.isValid(index,resource.toStack()); }
    protected boolean isValid(int index,ItemStack stack) { return true; }
    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        assert index >= 0 && index < this.storage.size();
        if(!this.isValid(index,resource))
            return 0;
        ItemStack insertStack = resource.toStack();
        ItemStack s = this.getStack(index);
        if(s.isEmpty() && this.isValid(index,resource))
        {
            //Insert up to our maximum allowed value
            int insertAmount = Math.min(amount,Math.min(this.getCapacityAsInt(index,resource),insertStack.getMaxStackSize()));
            if(insertAmount <= 0)
                return 0;
            this.updateSnapshots(transaction);
            this.storage.set(index,resource.toStack(insertAmount));
            this.afterChangeBeforeCommit(transaction);
            return insertAmount;
        }
        else if(ItemStack.isSameItemSameComponents(s,insertStack))
        {
            int space = Math.min(s.getMaxStackSize(),this.getCapacityAsInt(index,resource)) - s.getCount();
            int insertAmount = Math.min(amount,space);
            if(insertAmount <= 0)
                return 0;
            this.updateSnapshots(transaction);
            s.grow(insertAmount);
            this.afterChangeBeforeCommit(transaction);
            return insertAmount;
        }
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        assert index >= 0 && index < this.storage.size();
        ItemStack extractStack = resource.toStack();
        ItemStack s = this.getStack(index);
        if(s.isEmpty())
            return 0;
        if(ItemStack.isSameItemSameComponents(s,extractStack))
        {
            int removeAmount = Math.min(amount,s.getCount());
            this.updateSnapshots(transaction);
            s.shrink(removeAmount);
            this.afterChangeBeforeCommit(transaction);
            return removeAmount;
        }
        return 0;
    }

}
