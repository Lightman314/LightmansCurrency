package io.github.lightman314.lightmanscurrency.api.helpers.registry;

import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.*;
import java.util.function.*;

public final class AdvancedDeferredHolderBundle<K,R,T extends R> extends DeferredHolderBundle<K,R,T> {

    private final Function<K,DeferredHolder<R,T>> factory;
    @Override
    public AdvancedDeferredHolderBundle<K,R,T> lock() { super.lock(); return this; }

    public AdvancedDeferredHolderBundle(Comparator<K> sorter,Function<K,DeferredHolder<R,T>> factory) {
        super(sorter);
        this.factory = factory;
    }

    public void registerKey(K key) {
        DeferredHolder<R,T> holder = this.factory.apply(key);
        this.put(key,holder);
    }

}