package io.github.lightman314.lightmanscurrency.features.trader.item_common;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.ListBackedItemStorage;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;
import java.util.function.*;

public class FlexibleItemStorage extends ListBackedItemStorage {

    private final Predicate<ItemResource> filter;
    private final Supplier<Integer> capacity;
    public FlexibleItemStorage(Predicate<ItemResource> filter,Supplier<Integer> capacity,Runnable listener) { this(filter,capacity,(l1,l2) -> listener.run()); }
    public FlexibleItemStorage(Predicate<ItemResource> filter,Supplier<Integer> capacity,Consumer<Consumer<FancyPacketMap.Mutable>> listener) { this(filter,capacity,new PacketListener(listener)); }
    public FlexibleItemStorage(Predicate<ItemResource> filter,Supplier<Integer> capacity,ListChangedListener listener)
    {
        super(listener);
        this.filter = filter;
        this.capacity = capacity;
    }

    @Override
    public void copyFrom(List<ItemStack> contents) { super.copyFrom(ItemHelper.combineStacks(contents)); }

    @Override
    protected Codec<List<ItemStack>> codec() { return CodecHelper.UNLIMITED_ITEM_LIST; }

    public final int getCapacity() { return this.capacity.get(); }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) { return this.getCapacity(); }

    @Override
    public boolean isValid(int index,ItemResource resource) {
        ItemStack stack = this.getStack(index);
        if(stack.isEmpty())
            return this.filter.test(resource);
        return ItemStack.isSameItemSameComponents(stack,resource.toStack());
    }

    //Pretend we have more slots than we do so that other machines can acknowledge that we have space to insert more items
    @Override
    public int size() { return super.size() + 16; }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        //Stop now if the item is not allowed inside
        if(!this.filter.test(resource) || amount <= 0)
            return 0;
        //Check for other items to merge with
        for(ItemStack s : this.storage)
        {
            if(resource.matches(s))
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
        }
        //Otherwise add to the end of the list
        this.updateSnapshots(transaction);
        int insertAmount = Math.min(amount,this.capacity.get());
        this.storage.add(resource.toStack(insertAmount));
        this.afterChangeBeforeCommit(transaction);
        return insertAmount;
    }

    @Override
    public int extract(ItemResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        for(int i = 0; i < this.storage.size(); ++i)
        {
            ItemStack s = this.storage.get(i);
            if(resource.matches(s))
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
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        ItemStack s = this.getStack(index);
        if(s.isEmpty()) //If there's not already a present stack, use the slotless insert just in case another slot has this stack already
            return this.insert(resource,amount,transaction);
        //Stop here if the resource isn't allowed inside
        if(!this.filter.test(resource) || amount <= 0)
            return 0;
        //Otherwise, see if we can merge the two stacks
        ItemStack insertStack = resource.toStack();
        if(resource.matches(s))
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
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(amount <= 0)
            return 0;
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