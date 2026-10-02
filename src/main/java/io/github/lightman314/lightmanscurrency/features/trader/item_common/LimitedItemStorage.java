package io.github.lightman314.lightmanscurrency.features.trader.item_common;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.ListBackedItemStorage;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;
import java.util.function.Supplier;

public class LimitedItemStorage extends ListBackedItemStorage {

    private final Supplier<Integer> capacity;

    public LimitedItemStorage(Supplier<Integer> capacity,Runnable listener) { this(capacity,(l1,l2) -> listener.run()); }
    public LimitedItemStorage(Supplier<Integer> capacity,FancyPacketMap.Listener listener) { this(capacity,new PacketListener(listener)); }
    public LimitedItemStorage(Supplier<Integer> capacity,ListChangedListener listener) {
        super(listener);
        this.capacity = capacity;
    }

    @Override
    protected Codec<List<ItemStack>> codec() { return CodecHelper.UNLIMITED_ITEM_LIST; }

    public final int getCapacity() { return this.capacity.get(); }
    public final int getCurrentCount() {
        int count = 0;
        for(ItemStack s : this.storage)
            count += s.getCount();
        return count;
    }
    public final int getSpace() { return Math.max(0,this.getCapacity() - this.getCurrentCount()); }

    @Override
    public long getCapacityAsLong(int i, ItemResource resource) { return this.getCapacity(); }

    @Override
    public boolean isValid(int i,ItemResource resource) { return true; }

    //Pretend we have more slots than we do so that other machines can acknowledge that we have space to insert more items
    @Override
    public int size() { return super.size() + 16; }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        //Check how many we can insert
        int space = this.getSpace();
        if(space > 0 && amount > 0) {
            //Check for other items to merge with
            for(ItemStack s : this.storage) {
                if(resource.matches(s)) {
                    this.updateSnapshots(transaction);
                    int insertAmount = Math.min(space,amount);
                    s.grow(insertAmount);
                    this.afterChangeBeforeCommit(transaction);
                    return insertAmount;
                }
            }
            //Otherwise add to the end of the list
            this.updateSnapshots(transaction);
            int insertAmount = Math.min(space,amount);
            this.storage.add(resource.toStack(insertAmount));
            this.afterChangeBeforeCommit(transaction);
            return insertAmount;
        }
        return 0;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        ItemStack s = this.getStack(index);
        if(s.isEmpty()) //If the slot is empty, simply use the slotless insert just in case another slot has this stack already
            return this.insert(resource,amount,transaction);
        //Otherwise see if we can merge the two stacks
        if(amount <= 0)
            return 0;
        if(resource.matches(s)) {
            int space = this.getSpace();
            if(space > 0) {
                this.updateSnapshots(transaction);
                int insertAmount = Math.min(space,amount);
                s.grow(insertAmount);
                this.afterChangeBeforeCommit(transaction);
                return insertAmount;
            }
        }
        return 0;
    }

    @Override
    public int extract(ItemResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(amount <= 0)
            return 0;
        for(int i = 0; i < this.storage.size(); ++i) {
            ItemStack s = this.storage.get(i);
            if(resource.matches(s)) {
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
    public int extract(int index,ItemResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(amount <= 0)
            return 0;
        //Cannot remove if there's nothing here
        ItemStack s = this.getStack(index);
        if(s.isEmpty())
            return 0;
        if(resource.matches(s)) {
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
