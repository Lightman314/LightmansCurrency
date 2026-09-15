package io.github.lightman314.lightmanscurrency.api.helpers.resource;

import com.mojang.datafixers.util.Pair;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Supplier;

public sealed class DeferredResourceHandler<T extends Resource> implements ResourceHandler<T> {

    public static <T extends Resource> ResourceHandler<T> of(Supplier<? extends ResourceHandler<T>> source) { return new DeferredResourceHandler<>(source); }
    public static <T extends Resource> Pair<ResourceHandler<T>,IndexModifier<T>> of(Supplier<? extends ResourceHandler<T>> source,Supplier<? extends IndexModifier<T>> modifierSource) {
        WithModifier<T> result = new WithModifier<>(source,modifierSource);
        return Pair.of(result,result);
    }
    public static <X extends ResourceHandler<T> & IndexModifier<T>,T extends Resource> Pair<ResourceHandler<T>,IndexModifier<T>> ofBoth(Supplier<X> source) { return of(source,source); }

    private final Supplier<? extends ResourceHandler<T>> source;
    private DeferredResourceHandler(Supplier<? extends ResourceHandler<T>> source) {
        this.source = source;
    }

    private ResourceHandler<T> get() {
        ResourceHandler<T> result = this.source.get();
        return result == null ? EmptyResourceHandler.instance() : result;
    }

    @Override
    public int size() { return this.get().size(); }

    @Override
    public T getResource(int index) { return this.get().getResource(index); }

    @Override
    public long getAmountAsLong(int index) { return this.get().getAmountAsLong(index); }

    @Override
    public long getCapacityAsLong(int index, T resource) { return this.get().getCapacityAsLong(index,resource); }

    @Override
    public boolean isValid(int index, T resource) { return this.get().isValid(index,resource); }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) { return this.get().insert(index,resource,amount,transaction); }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) { return this.get().insert(resource,amount,transaction); }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) { return this.get().extract(index,resource,amount,transaction); }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) { return this.get().extract(resource,amount,transaction); }

    public WithModifier<T> withModifier(Supplier<IndexModifier<T>> modifierSource) { return new WithModifier<>(this.source,modifierSource); }

    public static final class WithModifier<T extends Resource> extends DeferredResourceHandler<T> implements IndexModifier<T> {
        private final Supplier<? extends IndexModifier<T>> modifierSource;
        private WithModifier(Supplier<? extends ResourceHandler<T>> source,Supplier<? extends IndexModifier<T>> modifierSource) {
            super(source);
            this.modifierSource = modifierSource;
        }
        @Override
        public void set(int index, T resource, int amount) { this.modifierSource.get().set(index,resource,amount); }
    }

}