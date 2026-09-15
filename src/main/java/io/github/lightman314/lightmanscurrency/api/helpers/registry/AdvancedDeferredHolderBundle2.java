package io.github.lightman314.lightmanscurrency.api.helpers.registry;

import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Comparator;
import java.util.function.BiFunction;

public class AdvancedDeferredHolderBundle2<K1,K2,R,T extends R> extends DeferredHolderBundle2<K1,K2,R,T> {

    private final Iterable<K2> keyCollection;
    private final BiFunction<K1,K2,DeferredHolder<R,T>> factory;

    public AdvancedDeferredHolderBundle2(Comparator<K1> sorter1,Comparator<K2> sorter2,Iterable<K2> keyCollection,BiFunction<K1,K2,DeferredHolder<R,T>> factory) {
        super(sorter1,sorter2);
        this.keyCollection = keyCollection;
        this.factory = factory;
    }

    public void registerKey(K1 key) {
        for(K2 key2 : this.keyCollection) {
            DeferredHolder<R,T> holder = this.factory.apply(key,key2);
            this.put(key,key2,holder);
        }
    }

}