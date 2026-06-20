package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.*;

/**
 * A basic implementation of {@link MoneyResourceHandler} that can store unlimited amounts of money of any type without restriction.<br>
 * Used in various places such as bank accounts and traders money storage.
 */
public class UnlimitedMoneyStorage implements MoneyResourceHandler {

    public static final Codec<UnlimitedMoneyStorage> CODEC = MoneyValue.CODEC.listOf().xmap(UnlimitedMoneyStorage::new,UnlimitedMoneyStorage::getAllResources);
    public static final StreamCodec<RegistryFriendlyByteBuf,UnlimitedMoneyStorage> STREAM_CODEC = MoneyValue.STREAM_CODEC.apply(ByteBufCodecs.list()).map( UnlimitedMoneyStorage::new,UnlimitedMoneyStorage::getAllResources);

    private final Journal snapshots = new Journal();
    private final Map<MoneyKey,MoneyValue> storage = new HashMap<>();
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

    @Override
    public MoneyValue insert(MoneyValue value, TransactionContext transaction) {
        //Can't insert anything if the value is empty
        if(value.isEmpty())
            return MoneyValue.empty();
        //Get the current value
        MoneyValue currentValue = this.getResource(value.getKey());
        //Add the new value to the current value
        MoneyValue newValue = currentValue.addValue(value);
        //Abort if the math failed
        if(newValue == null || !newValue.getKey().equals(currentValue.getKey()))
            return MoneyValue.empty();
        //Store the snapshot so we can revert the changes
        this.snapshots.updateSnapshots(transaction);
        //Put the new value in storage
        this.storage.put(newValue.getKey(),newValue);
        return value;
    }

    @Override
    public MoneyValue extract(MoneyValue value, TransactionContext transaction) {
        //Can't extract anything if the value is empty
        if(value.isEmpty())
            return MoneyValue.empty();
        //Get the current value
        MoneyValue currentValue = this.getResource(value.getKey());
        //Get the amount we can extract
        MoneyValue extractAmount = currentValue.containsValue(value) ? value : currentValue;
        //Calculate the new value
        MoneyValue newValue = currentValue.subtractValue(extractAmount);
        //Abort if the math failed
        if(newValue == null || !newValue.getKey().equals(currentValue.getKey()))
            return MoneyValue.empty();
        //Store the snapshot so we can revert the changes
        this.snapshots.updateSnapshots(transaction);
        //Clear the storage of that key if the value is now empty
        if(newValue.isEmpty())
            this.storage.remove(currentValue.getKey());
        else //Put the new value in storage
            this.storage.put(newValue.getKey(),newValue);
        //Return the amount extracted
        return extractAmount;
    }

    private class Journal extends SnapshotJournal<Map<MoneyKey, MoneyValue>> {
        //Just copy the entire contents
        @Override
        protected Map<MoneyKey, MoneyValue> createSnapshot() { return ImmutableMap.copyOf(UnlimitedMoneyStorage.this.storage); }
        @Override
        protected void revertToSnapshot(Map<MoneyKey,MoneyValue> snapshot) {
            UnlimitedMoneyStorage.this.storage.clear();
            UnlimitedMoneyStorage.this.storage.putAll(snapshot);
        }
        @Override
        protected void onRootCommit(Map<MoneyKey, MoneyValue> originalState) {
            super.onRootCommit(originalState);
            //Set changed
            for(Runnable l : new ArrayList<>(UnlimitedMoneyStorage.this.listeners))
                l.run();
        }
    }

}
