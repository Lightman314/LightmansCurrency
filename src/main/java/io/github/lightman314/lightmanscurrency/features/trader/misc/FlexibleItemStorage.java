package io.github.lightman314.lightmanscurrency.features.trader.misc;

import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.ListBackedItemStorage;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;
import java.util.function.*;

public class FlexibleItemStorage extends ListBackedItemStorage {

    private final Predicate<ItemStack> filter;
    private final Supplier<Integer> capacity;
    public FlexibleItemStorage(Predicate<ItemStack> filter,Supplier<Integer> capacity,Runnable listener) { this(filter,capacity,(l1,l2) -> listener.run()); }
    public FlexibleItemStorage(Predicate<ItemStack> filter,Supplier<Integer> capacity,BiConsumer<List<ItemStack>,List<ItemStack>> listener)
    {
        super(listener);
        this.filter = filter;
        this.capacity = capacity;
    }

    @Override
    public void copyFrom(List<ItemStack> contents) { super.copyFrom(ItemHelper.combineStacks(contents)); }

    public final int getCapacity() { return this.capacity.get(); }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) { return this.getCapacity(); }

    @Override
    public boolean isValid(int index,ItemResource resource) {
        ItemStack stack = this.getStack(index);
        if(stack.isEmpty())
            return this.filter.test(resource.toStack());
        return ItemStack.isSameItemSameComponents(stack,resource.toStack());
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        ItemStack insertedStack = resource.toStack();
        //Stop now if the item is not allowed inside
        if(!this.filter.test(insertedStack))
            return 0;
        //Check for other items to merge with
        for(ItemStack s : this.storage)
        {
            if(ItemStack.isSameItemSameComponents(s,insertedStack))
            {
                int space = capacity.get() - s.getCount();
                if(space > 0)
                {
                    this.updateSnapshots(transaction);
                    int insertAmount = Math.min(space,amount);
                    s.grow(insertAmount);
                    return insertAmount;
                }
                //If space is <= 0, we cannot fit this item
                return 0;
            }
        }
        //Otherwise add to the end of the list
        this.updateSnapshots(transaction);
        int insertAmount = Math.min(insertedStack.getCount(),this.capacity.get());
        this.storage.add(insertedStack.copyWithCount(insertAmount));
        this.afterChangeBeforeCommit(transaction);
        return insertAmount;
    }

    @Override
    public int extract(ItemResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        ItemStack extractStack = resource.toStack();
        for(int i = 0; i < this.storage.size(); ++i)
        {
            ItemStack s = this.storage.get(i);
            if(ItemStack.isSameItemSameComponents(s,extractStack))
            {
                this.updateSnapshots(transaction);
                int removeAmount = Math.min(s.getCount(),amount);
                s.shrink(removeAmount);
                if(s.isEmpty())
                    this.storage.remove(i);
                this.afterChangeBeforeCommit(transaction);
                return removeAmount;
            }
        }
        return 0;
    }

    @Override
    public int insert(int index,ItemResource resource,int amount,TransactionContext transaction) {
        ItemStack s = this.getStack(index);
        if(s.isEmpty()) //If there's not already a present stack, use the slotless insert just in case another slot has this stack already
            return this.insert(resource,amount,transaction);
        //Otherwise, see if we can merge the two stacks
        ItemStack insertStack = resource.toStack();
        if(ItemStack.isSameItemSameComponents(s,insertStack))
        {
            int space = this.capacity.get() - s.getCount();
            if(space > 0)
            {
                this.updateSnapshots(transaction);
                int insertAmount = Math.min(space,amount);
                s.grow(insertAmount);
                this.afterChangeBeforeCommit(transaction);
                return insertAmount;
            }
            //If space is <= 0, we cannot fit this item
            return 0;
        }
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        ItemStack s = this.getStack(index);
        //Cannot remove if the stack is already empty
        if(s.isEmpty())
            return 0;
        ItemStack extractStack = resource.toStack();
        if(ItemStack.isSameItemSameComponents(s,extractStack))
        {
            this.updateSnapshots(transaction);
            int removeAmount = Math.min(s.getCount(),amount);
            s.shrink(removeAmount);
            if(s.isEmpty())
                this.storage.remove(index);
            this.afterChangeBeforeCommit(transaction);
            return removeAmount;
        }
        //Cannot extract from this slot if the items aren't the same
        return 0;
    }

}