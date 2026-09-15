package io.github.lightman314.lightmanscurrency.api.helpers.resource;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * An abstract {@link ResourceHandler} for items that is backed by a non-null list<br>
 * Extensions are free to add/remove entries from the list, and this has built-in helpers for easy and somewhat effecient sync packets via {@link #createPacket(List, List)} and {@link #processPacket(FancyPacketMap)}<br>
 * Can be easily encoded via {@link #getContents()} and decoded via {@link #copyFrom(List)}<br>
 * Custom implementations may wish to overwrite {@link #copyFrom(List)} if they wish to enforce a predefined number of item slots
 */
public abstract class ListBackedItemStorage implements ResourceHandler<ItemResource> {

    protected final List<ItemStack> storage;
    private final Snapshot snapshot = new Snapshot();

    private ListChangedListener listener = (l1,l2) -> {};
    public void setListener(Runnable listener) { this.setListener((l1,l2) -> listener.run());}
    public void setListener(ListChangedListener listener) { this.listener = listener; }
    public void setListener(Consumer<Consumer<FancyPacketMap.Mutable>> listener) { this.listener = new PacketListener(listener); }

    public ListBackedItemStorage(List<ItemStack> list) { this.storage = new ArrayList<>(list); }
    public ListBackedItemStorage(Runnable listener) { this((l1,l2) -> listener.run()); }
    public ListBackedItemStorage(ListChangedListener listener) { this.listener = listener; this.storage = new ArrayList<>(); }

    public void copyFrom(List<ItemStack> list)
    {
        this.storage.clear();
        List<ItemStack> copy = ItemHelper.copyList(list);
        ItemHelper.validateList(copy);
        this.storage.addAll(copy);
    }

    public final boolean isEmpty() { return this.storage.stream().allMatch(ItemStack::isEmpty); }

    //Shouldn't need to exagerate the size since the slotless insert method is now present for inserting items into
    @Override
    public int size() { return this.storage.size(); }

    public final ItemStack getStack(int index) { return ItemHelper.safeGetStack(this.storage,index); }

    @Override
    public ItemResource getResource(int index) { return ItemResource.of(this.getStack(index)); }
    @Override
    public long getAmountAsLong(int index) { return this.getStack(index).getCount(); }

    public final List<ItemStack> getContents() { return ItemHelper.copyList(this.storage); }

    protected abstract Codec<List<ItemStack>> codec();

    public void serialize(ValueOutput valueOutput,String key) {
        valueOutput.store(key,this.codec(),this.storage);
    }

    public void deserialize(ValueInput valueInput,String key) {
        valueInput.read(key,this.codec()).ifPresent(this::copyFrom);
    }

    public static Consumer<FancyPacketMap.Mutable> createPacket(List<ItemStack> oldState, List<ItemStack> newState)
    {
        return map -> {
            for(int i = 0; i < newState.size() || i < oldState.size(); ++i)
            {
                if(i >= oldState.size())
                {
                    //Simply append an "add stack" packet
                    map.setMap(String.valueOf(i),FancyPacketMap.map()
                            .setEnum("operation",UpdateType.SET)
                            .setItem("value",newState.get(i)));
                }
                else if(i >= newState.size())
                {
                    //Send a "clear stack" packet
                    map.setMap(String.valueOf(i),FancyPacketMap.map()
                            .setEnum("operation",UpdateType.REMOVE));
                }
                else
                {
                    ItemStack oldStack = oldState.get(i);
                    ItemStack newStack = newState.get(i);
                    if(!ItemStack.matches(oldStack,newStack))
                    {
                        //Send an "update stack" packet
                        map.setMap(String.valueOf(i),FancyPacketMap.map()
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
        Set<Integer> removeIndex = new HashSet<>();
        for(String key : map.keySet()) {
            FancyPacketMap entry = map.getMap(key);
            int slot = NumberHelper.getInteger(key,-1);
            if(slot < 0)
                continue;
            ListBackedItemStorage.UpdateType action = entry.getEnum("operation", ListBackedItemStorage.UpdateType.class);
            if(action == null) {
                continue;
            }
            if(action == ListBackedItemStorage.UpdateType.REMOVE)
            {
                //Remove all indexes past this one just to be sure
                while(contents.size() > slot)
                    contents.remove(slot);
            }
            if(action == ListBackedItemStorage.UpdateType.SET)
            {
                ItemStack newStack = entry.getItem("value",false);
                //Force the list to be a valid size
                while(slot >= contents.size())
                    contents.add(ItemStack.EMPTY);
                contents.set(slot,newStack);
            }
        }
        //Load from the new contents list
        this.copyFrom(contents);
    }

    protected void afterChangeBeforeCommit(TransactionContext transaction) { }

    protected void afterChangeCommit() { }

    protected final void setChanged(List<ItemStack> oldState)
    {
        //Flag as changed after the commit
        if(!ItemHelper.listsMatch(this.storage,oldState))
            this.knownChange(oldState);
    }

    protected final void knownChange(List<ItemStack> oldState) {
        this.afterChangeCommit();
        this.listener.afterContentsChanged(oldState,ItemHelper.copyList(this.getContents()));
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

    public interface ListChangedListener {
        void afterContentsChanged(List<ItemStack> oldContents,List<ItemStack> newContents);
    }

    public record PacketListener(Consumer<Consumer<FancyPacketMap.Mutable>> writer) implements ListChangedListener {
        @Override
        public void afterContentsChanged(List<ItemStack> oldContents, List<ItemStack> newContents) {
            this.writer.accept(createPacket(oldContents,newContents));
        }
    }

}
