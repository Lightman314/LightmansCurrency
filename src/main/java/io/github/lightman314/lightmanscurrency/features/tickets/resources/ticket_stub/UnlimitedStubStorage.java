package io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketHelper;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class UnlimitedStubStorage extends SnapshotJournal<Void> implements TicketStubCollector, FancyPacketMap.FancyPacketWriter {

    public static final Codec<UnlimitedStubStorage> CODEC = Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(),Codec.LONG)
            .xmap(UnlimitedStubStorage::new,s -> s.storage);
    public static final StreamCodec<RegistryFriendlyByteBuf,UnlimitedStubStorage> STREAM_CODEC = StreamHelper.map(ByteBufCodecs.registry(Registries.ITEM),ByteBufCodecs.LONG)
            .map(UnlimitedStubStorage::new,s -> s.storage);

    private FancyPacketMap.Listener listener = b -> {};
    public UnlimitedStubStorage withListener(Runnable listener) { return this.withListener(b -> listener.run()); }
    public UnlimitedStubStorage withListener(FancyPacketMap.Listener listener) { this.listener = listener; return this; }

    private final Map<Item,Long> storage = new HashMap<>();
    private final Map<Item,Snapshot> snapshots = new HashMap<>();

    public UnlimitedStubStorage() { }

    private UnlimitedStubStorage(Map<Item,Long> storage) { this.storage.putAll(storage); }

    public void copyFrom(UnlimitedStubStorage other) {
        this.storage.clear();
        this.storage.putAll(other.storage);
    }

    public boolean isEmpty() { return this.storage.isEmpty(); }
    public long getStubCount() {
        long count = 0;
        for(var c : this.storage.values())
            count += c;
        return count;
    }
    public long getStubCount(Item stub) { return this.storage.getOrDefault(stub,0L); }

    @Override
    public int collectTicketStub(ItemStack destroyedTicket,int count,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(ItemResource.of(destroyedTicket),count);
        Item stub = TicketHelper.getTicketStub(destroyedTicket);
        if(stub != null) {
            this.snapshots.computeIfAbsent(stub,Snapshot::new).updateSnapshots(transaction);
            this.updateSnapshots(transaction);
            long stored = this.storage.getOrDefault(stub,0L);
            this.storage.put(stub,stored + count);
            return count;
        }
        return 0;
    }

    public void giveToPlayer(Player player) {
        Inventory inventory = player.getInventory();
        this.dumpContents(inventory::placeItemBackInInventory,true);
    }
    public void dumpContents(Consumer<ItemStack> consumer,boolean setChanged) {
        for(Item stub : Set.copyOf(this.storage.keySet())) {
            //Give the stub to the player
            long count = this.getStubCount(stub);
            ItemStack stack = new ItemStack(stub);
            stack.setCount(stack.getMaxStackSize());
            while(count > stack.getMaxStackSize()) {
                count -= stack.getMaxStackSize();
                consumer.accept(stack.copy());
            }
            if(count > 0) {
                stack.setCount(Ints.saturatedCast(count));
                consumer.accept(stack);
            }
            //Remove from storage
            this.storage.remove(stub);
            if(setChanged)
                this.setChanged(stub);
        }
    }

    public void handlePacket(FancyPacketMap packet) {
        for(String key : packet.keySet()) {
            try {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(key));
                if(item != Items.AIR) {
                    long count = packet.getLong(key);
                    if(count <= 0)
                        this.storage.remove(item);
                    else
                        this.storage.put(item,count);
                }
            } catch (IdentifierException ignored) {}
        }
    }

    @Override
    protected Void createSnapshot() { return null; }
    @Override
    protected void revertToSnapshot(Void snapshot) { }

    private void setChanged(Item stub) {
        this.listener.accept(b -> b.setLong(BuiltInRegistries.ITEM.getKey(stub).toString(),this.getStubCount(stub)));
    }

    @Override
    public void writeToPacket(FancyPacketMap.Mutable packet) {
        this.storage.forEach((stub,count) ->
            packet.setLong(BuiltInRegistries.ITEM.getKey(stub).toString(),count));
    }

    private class Snapshot extends SnapshotJournal<Long> {
        private final Item stub;
        private Snapshot(Item stub) { this.stub = stub; }
        @Override
        protected Long createSnapshot() { return UnlimitedStubStorage.this.storage.getOrDefault(this.stub,0L); }
        @Override
        protected void revertToSnapshot(Long snapshot) {}
        @Override
        protected void onRootCommit(Long originalState) {
            if(UnlimitedStubStorage.this.getStubCount(this.stub) != originalState)
                UnlimitedStubStorage.this.setChanged(this.stub);
        }
    }

}
