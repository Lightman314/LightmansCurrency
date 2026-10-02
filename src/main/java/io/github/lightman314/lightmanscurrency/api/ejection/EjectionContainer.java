package io.github.lightman314.lightmanscurrency.api.ejection;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.*;

public final class EjectionContainer extends SnapshotJournal<Set<Long>> implements ResourceHandler<ItemResource>, IndexModifier<ItemResource>, ISidedContext {

    private final Player player;

    @Override
    public boolean isClient() { return player.level().isClientSide(); }

    public EjectionContainer(Player player) { this.player = player; }

    private Set<Long> changed = new HashSet<>();
    private final Map<Long,EntrySnapshot> snapshots = new HashMap<>();

    private EntrySnapshot getSnapshot(EjectionEntry entry) { return this.snapshots.computeIfAbsent(entry.getID(),EntrySnapshot::new); }

    private List<ItemStack> getAccessibleContents() {
        List<ItemStack> contents = new ArrayList<>();
        for(EjectionEntry entry : LCApi.getEjectionAPI().getDataForPlayer(this.player)) {
            contents.addAll(entry.getContents());
        }
        return contents;
    }

    @Nullable
    private Pair<EjectionEntry,Integer> getEntryForIndex(int index) {
        for(EjectionEntry entry : LCApi.getEjectionAPI().getDataForPlayer(this.player)) {
            if(index < entry.getContents().size())
                return Pair.of(entry,index);
        }
        return null;
    }

    @Override
    public int size() { return this.getAccessibleContents().size(); }
    @Override
    public ItemResource getResource(int index) { return ItemResource.of(ListHelper.getOrDefault(this.getAccessibleContents(),index,ItemStack.EMPTY)); }
    @Override
    public long getAmountAsLong(int index) { return ListHelper.getOrDefault(this.getAccessibleContents(),index,ItemStack.EMPTY).getCount(); }
    @Override
    public long getCapacityAsLong(int index, ItemResource resource) { return 0; }
    @Override
    public boolean isValid(int index, ItemResource resource) { return false; }
    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }

    @Override
    public int extract(int index,ItemResource resource,int amount,TransactionContext transaction) {
        Pair<EjectionEntry,Integer> data = this.getEntryForIndex(index);
        if(data == null)
            return 0;
        EjectionEntry entry = data.getFirst();
        index = data.getSecond();
        ItemStack stack = entry.getContents().get(index);
        if(resource.matches(stack)) {
            int removed = Math.min(amount,stack.getCount());
            //Store the data to the changed set before updating the local snapshot
            this.changed.add(entry.getID());
            this.updateSnapshots(transaction);
            //Update the snapshot of the entries contents
            this.getSnapshot(entry).updateSnapshots(transaction);
            //Shrink the stack
            stack.shrink(removed);
            return removed;
        }
        return 0;
    }

    @Override
    public void set(int index,ItemResource resource,int amount) {
        Pair<EjectionEntry,Integer> data = this.getEntryForIndex(index);
        if(data != null) {
            EjectionEntry entry = data.getFirst();
            index = data.getSecond();
            List<ItemStack> contents = entry.getContents();
            contents.set(index,resource.toStack(amount));
            entry.setChanged();
        }
    }

    @Override
    protected Set<Long> createSnapshot() { return new HashSet<>(this.changed); }

    @Override
    protected void revertToSnapshot(Set<Long> snapshot) { this.changed = snapshot; }

    @Override
    protected void onRootCommit(Set<Long> originalState) {
        //Flag everything is changed after the root commit
        for(long changed : this.changed) {
            EjectionEntry entry = LCApi.getEjectionAPI().getEntry(this,changed);
            if(entry != null)
                entry.setChanged();
        }
        this.changed = new HashSet<>();
    }

    private class EntrySnapshot extends SnapshotJournal<List<ItemStack>> {

        private final long id;
        public EntrySnapshot(long id) { this.id = id; }
        @Nullable
        private EjectionEntry getEntry() { return LCApi.getEjectionAPI().getEntry(EjectionContainer.this,this.id); }
        @Override
        protected List<ItemStack> createSnapshot() {
            EjectionEntry entry = this.getEntry();
            if(entry != null)
                return ItemHelper.copyList(entry.getContents());
            return List.of();
        }
        @Override
        protected void revertToSnapshot(List<ItemStack> snapshot) {
            EjectionEntry entry = this.getEntry();
            if(entry != null)
                entry.updateContents(snapshot);
        }
    }

}
