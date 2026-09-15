package io.github.lightman314.lightmanscurrency.api.helpers.resource;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * My own implementation of {@link net.neoforged.neoforge.transfer.CombinedResourceHandler CombinedResourceHandler} that is capable of properly acknowledging resource handlers with flexible sizes
 */
public class FlexibleCombinedResourceHandler<T extends Resource> implements ResourceHandler<T> {

    private final List<ResourceHandler<T>> resourceHandlers;

    public FlexibleCombinedResourceHandler(Collection<ResourceHandler<T>> resourceHandlers) {
        this.resourceHandlers = List.copyOf(resourceHandlers);
    }

    @Nullable
    private Pair<ResourceHandler<T>,Integer> getTarget(int index) {
        if(index < 0)
            throw new IndexOutOfBoundsException(index);
        for(ResourceHandler<T> r : this.resourceHandlers) {
            int size = r.size();
            if(index < size)
                return Pair.of(r,index);
            index -= size;
        }
        return null;
    }

    private Pair<ResourceHandler<T>,Integer> getTargetOrThrow(int index) {
        var result = this.getTarget(index);
        if(result == null)
            throw new IndexOutOfBoundsException(index);
        return result;
    }

    @Override
    public int size() {
        int size = 0;
        for(ResourceHandler<T> r : this.resourceHandlers)
            size += r.size();
        return size;
    }

    @Override
    public T getResource(int index) {
        var entry = this.getTargetOrThrow(index);
        return entry.getFirst().getResource(entry.getSecond());
    }

    @Override
    public long getAmountAsLong(int index) {
        var entry = this.getTarget(index);
        return entry == null ? 0 : entry.getFirst().getAmountAsLong(entry.getSecond());
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        var entry = this.getTarget(index);
        return entry == null ? 0 : entry.getFirst().getCapacityAsLong(entry.getSecond(),resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        var entry = this.getTarget(index);
        return entry != null && entry.getFirst().isValid(entry.getSecond(), resource);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int inserted = 0;
        int pending = amount;
        for(ResourceHandler<T> r : this.resourceHandlers) {
            try(Transaction tx = Transaction.open(transaction)) {
                int i = r.insert(resource,pending,tx);
                if(i <= pending) {
                    tx.commit();
                    inserted += i;
                    pending -= i;
                    if(pending == 0)
                        return inserted;
                }
            }
        }
        return inserted;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var entry = this.getTarget(index);
        return entry == null ? 0 : entry.getFirst().insert(entry.getSecond(),resource,amount,transaction);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int extracted = 0;
        int pending = amount;
        for(ResourceHandler<T> r : this.resourceHandlers) {
            try(Transaction tx = Transaction.open(transaction)) {
                int e = r.extract(resource,pending,tx);
                if(e <= pending) {
                    tx.commit();
                    extracted += e;
                    pending -= e;
                    if(pending == 0)
                        return extracted;
                }
            }
        }
        return extracted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var entry = this.getTarget(index);
        return entry == null ? 0 : entry.getFirst().extract(entry.getSecond(),resource,amount,transaction);
    }

}
