package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.*;

/**
 * A basic implementation of {@link MoneyResourceHandler} that can store unlimited amounts of money of any type without restriction.<br>
 * Used in various places such as bank accounts and traders money storage.
 */
public class UnlimitedMoneyStorage extends SnapshotJournal<Unit> implements MoneyResourceHandler {

    public static final Codec<UnlimitedMoneyStorage> CODEC = MoneyValue.CODEC.listOf().xmap(UnlimitedMoneyStorage::new,UnlimitedMoneyStorage::getAllResources);
    public static final StreamCodec<RegistryFriendlyByteBuf,UnlimitedMoneyStorage> STREAM_CODEC = MoneyValue.STREAM_CODEC.apply(ByteBufCodecs.list()).map( UnlimitedMoneyStorage::new,UnlimitedMoneyStorage::getAllResources);

    private final Map<MoneyKey,MoneyValue> storage = new HashMap<>();
    private final Map<MoneyKey,Snapshot> snapshots = new HashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();

    public UnlimitedMoneyStorage() {}

    protected UnlimitedMoneyStorage(List<MoneyValue> values) { this.copyFrom(values); }

    public void copyFrom(UnlimitedMoneyStorage storage) { this.copyFrom(storage.storage.values()); }
    public void copyFrom(Collection<MoneyValue> values)
    {
        this.storage.clear();
        for(MoneyValue value : values)
        {
            if(!value.isEmpty() && !value.isFree())
                this.storage.put(value.getKey(),value);
        }
    }

    public final UnlimitedMoneyStorage withListener(Runnable listener)
    {
        if(!this.listeners.contains(listener))
            this.listeners.add(listener);
        return this;
    }

    @Override
    public List<MoneyValue> getAllResources() { return ImmutableList.copyOf(this.storage.values()); }

    @Override
    public MoneyValue getResource(MoneyKey key) { return this.storage.getOrDefault(key,MoneyValue.empty()); }

    private void updateSnapshots(MoneyKey key,TransactionContext transaction) {
        Snapshot s = this.snapshots.computeIfAbsent(key,Snapshot::new);
        s.updateSnapshots(transaction);
        this.updateSnapshots(transaction);
    }

    @Override
    public MoneyValue insert(MoneyValue value,TransactionContext transaction) {
        TransferPreconditions.checkNonEmpty(value);
        //Can't insert anything if the value is empty
        if(value.isEmpty())
            return MoneyValue.empty();
        MoneyKey key = value.getKey();
        //Get the current value
        MoneyValue currentValue = this.getResource(key);
        //Add the new value to the current value
        MoneyValue newValue = currentValue.addValue(value);
        //Abort if the math failed or the resulting key is different from the desired key
        if(newValue == null || !newValue.getKey().equals(key))
            return MoneyValue.empty();
        //Store the snapshot so we can revert the changes
        this.updateSnapshots(key,transaction);
        //Put the new value in storage
        this.storage.put(key,newValue);
        return value;
    }

    @Override
    public MoneyValue extract(MoneyValue value, TransactionContext transaction) {
        //Can't extract anything if the value is empty
        if(value.isEmpty())
            return MoneyValue.empty();
        MoneyKey key = value.getKey();
        //Get the current value
        MoneyValue currentValue = this.getResource(key);
        //Get the amount we can extract
        MoneyValue extractAmount = currentValue.containsValue(value) ? value : currentValue;
        //Calculate the new value
        MoneyValue newValue = currentValue.subtractValue(extractAmount);
        //Abort if the math failed
        if(newValue == null || (!newValue.isEmpty() && !newValue.getKey().equals(key)))
            return MoneyValue.empty();
        //Store the snapshot so we can revert the changes
        this.updateSnapshots(key,transaction);
        //Clear the storage of that key if the value is now empty
        if(newValue.isEmpty())
            this.storage.remove(key);
        else //Put the new value in storage
            this.storage.put(key,newValue);
        //Return the amount extracted
        return extractAmount;
    }

    @Override
    protected Unit createSnapshot() { return Unit.INSTANCE; }

    @Override
    protected void revertToSnapshot(Unit snapshot) { }

    @Override
    protected void onRootCommit(Unit state) {
        for(Runnable l : new ArrayList<>(this.listeners))
            l.run();
    }

    private class Snapshot extends SnapshotJournal<MoneyValue> {

        private final MoneyKey key;
        private Snapshot(MoneyKey key) { this.key = key; }

        @Override
        protected MoneyValue createSnapshot() { return UnlimitedMoneyStorage.this.getResource(this.key); }

        @Override
        protected void revertToSnapshot(MoneyValue snapshot) {
            if(snapshot.isEmpty())
                UnlimitedMoneyStorage.this.storage.remove(this.key);
            else
                UnlimitedMoneyStorage.this.storage.put(this.key,snapshot);
        }
    }

}
