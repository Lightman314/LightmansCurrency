package io.github.lightman314.lightmanscurrency.api.helpers.resource;

import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An abstract {@link ResourceHandler} for items that is backed by a non-null list<br>
 * Extensions are free to add/remove entries from the list, and this has built-in helpers for easy and somewhat effecient sync packets via {@link #createPacket(List, List)} and {@link #processPacket(FancyPacketMap)}<br>
 * Can be easily encoded via {@link #getContents()} and decoded via {@link #copyFrom(List)}<br>
 * Custom implementations may wish to overwrite {@link #copyFrom(List)} if they wish to enforce a predefined number of item slots
 */
public abstract class ListBackedItemStorage implements ResourceHandler<ItemResource> {

    protected final NonNullList<ItemStack> storage;
    private final Snapshot snapshot = new Snapshot();

    private BiConsumer<List<ItemStack>,List<ItemStack>> listener = (l1,l2) -> {};
    public void setListener(Runnable listener) { this.setListener((l1,l2) -> listener.run());}
    public void setListener(BiConsumer<List<ItemStack>,List<ItemStack>> listener) { this.listener = listener; }

    public ListBackedItemStorage(NonNullList<ItemStack> list) { this.storage = list; }
    public ListBackedItemStorage(Runnable listener) { this((l1,l2) -> listener.run()); }
    public ListBackedItemStorage(BiConsumer<List<ItemStack>,List<ItemStack>> listener) { this.listener = listener; this.storage = NonNullList.of(ItemStack.EMPTY); }

    public void copyFrom(List<ItemStack> list)
    {
        this.fullClearList();
        this.storage.addAll(ItemHelper.copyList(list));
    }

    protected final void fullClearList() {
        while(!this.storage.isEmpty())
            this.storage.removeFirst();
    }

    //Shouldn't need to exagerate the size since the slotless insert method is now present for inserting items into
    @Override
    public int size() { return this.storage.size(); }

    public final ItemStack getStack(int index) { return ItemHelper.safeGetStack(this.storage,index); }

    @Override
    public ItemResource getResource(int index) { return ItemResource.of(this.getStack(index)); }
    @Override
    public long getAmountAsLong(int index) { return this.getStack(index).getCount(); }

    public final List<ItemStack> getContents() { return ItemHelper.copyList(this.storage); }

    public final Consumer<FancyPacketMap.Mutable> createPacket(List<ItemStack> oldState, List<ItemStack> newState)
    {
        return map -> {
            for(int i = 0; i < newState.size() || i < oldState.size(); ++i)
            {
                if(i >= oldState.size())
                {
                    //Simply append an "add stack" packet
                    map.setMap(String.valueOf(i),FancyPacketMap.newMutable()
                            .setEnum("operation",UpdateType.SET)
                            .setItem("value",newState.get(i)));
                }
                else if(i >= newState.size())
                {
                    //Send a "clear stack" packet
                    map.setMap(String.valueOf(i),FancyPacketMap.newMutable()
                            .setEnum("operation",UpdateType.REMOVE));
                }
                else
                {
                    ItemStack oldStack = oldState.get(i);
                    ItemStack newStack = newState.get(i);
                    if(!ItemStack.isSameItemSameComponents(oldStack,newStack) || oldStack.getCount() != newStack.getCount())
                    {
                        //Send an "update stack" packet
                        map.setMap(String.valueOf(i),FancyPacketMap.newMutable()
                                .setEnum("operation",UpdateType.SET)
                                .setItem("value",newStack));
                    }
                }
            }
        };
    }

    public final void processPacket(FancyPacketMap map)
    {
        List<ItemStack> contents = this.getContents();
        Set<String> keys = new HashSet<>(map.keySet());
        Set<Integer> removeIndex = new HashSet<>();
        keys.removeIf(key -> NumberHelper.getInteger(key,-1) < 0);
        for(int i = 0; !keys.isEmpty(); i++)
        {
            String indexString = String.valueOf(i);
            if(keys.remove(indexString))
            {
                FancyPacketMap entry = map.getMap(indexString);
                ListBackedItemStorage.UpdateType action = entry.getEnum("action", ListBackedItemStorage.UpdateType.class);
                if(action == null)
                    continue;
                if(action == ListBackedItemStorage.UpdateType.REMOVE)
                {
                    //Remove all indexes past this one just to be sure
                    while(contents.size() >= i)
                        contents.remove(i);
                }
                if(action == ListBackedItemStorage.UpdateType.SET)
                {
                    ItemStack newStack = entry.getItem("value");
                    if(newStack.isEmpty())
                        continue; //Cannot set to an empty stack for this particular form of item storage
                    if(i <= contents.size())
                        contents.set(i,newStack);
                }
            }
        }
        //Load from the new contents list
        this.copyFrom(contents);
    }

    protected final void setChanged(List<ItemStack> oldState)
    {
        //Flag as changed after the commit
        if(!ItemHelper.listsMatch(this.storage,oldState))
            this.listener.accept(oldState,ItemHelper.copyList(ListBackedItemStorage.this.getContents()));
    }

    protected final void updateSnapshots(TransactionContext context) { this.snapshot.updateSnapshots(context); }

    protected final class Snapshot extends SnapshotJournal<List<ItemStack>>
    {
        @Override
        protected List<ItemStack> createSnapshot() { return ItemHelper.copyList(ListBackedItemStorage.this.storage); }
        @Override
        protected void revertToSnapshot(List<ItemStack> snapshot) { ListBackedItemStorage.this.copyFrom(snapshot); }
        @Override
        protected void onRootCommit(List<ItemStack> originalState) {
            ListBackedItemStorage.this.setChanged(originalState);
        }
    }

    public enum UpdateType {
        SET,REMOVE
    }
}
