package io.github.lightman314.lightmanscurrency.api.helpers.registry;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.helpers.system.Consumer3;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DeferredHolderBundle2<K1,K2,R,T extends R> {

    private final Comparator<K1> sorter1;
    private final Comparator<K2> sorter2;

    private boolean locked = false;
    public DeferredHolderBundle2<K1,K2,R,T> lock() {
        this.locked = true;
        ImmutableList.Builder<DeferredHolder<R,T>> builder = ImmutableList.builder();
        this.values.forEach((k1,subMap) ->
                subMap.forEach((k2,value) ->
                        builder.add(value)));
        this.allValues = builder.build();
        return this;
    }

    public DeferredHolderBundle2(Comparator<K1> sorter1,Comparator<K2> sorter2) { this.sorter1 = sorter1; this.sorter2 = sorter2; }

    private final Map<K1, Map<K2, DeferredHolder<R,T>>> values = new HashMap<>();
    private ImmutableList<DeferredHolder<R,T>> allValues = ImmutableList.of();

    private Map<K2,DeferredHolder<R,T>> getSubmap(K1 key1) {
        if(!this.values.containsKey(key1))
            this.values.put(key1,new HashMap<>());
        return this.values.get(key1);
    }

    public void put(K1 key1,K2 key2,DeferredHolder<R,T> value) {
        if(this.locked)
            throw new IllegalStateException("Cannot add new values to the bundle after it's been locked!");
        Map<K2,DeferredHolder<R,T>> submap = this.getSubmap(key1);
        if(submap.containsKey(key2))
            throw new IllegalStateException("Attempted to add an entry of key " + key1 + " & " + key2 + " when an entry is already present in the bundle!");
        submap.put(key2,value);
    }

    public DeferredHolder<R,T> getHolder(K1 key1, K2 key2) { return this.getSubmap(key1).get(key2); }
    public T get(K1 key1, K2 key2) { return this.getHolder(key1,key2).get(); }

    public Collection<DeferredHolder<R,T>> getAllHolders() { return this.allValues; }
    public Collection<T> getAll() { return this.allValues.stream().map(DeferredHolder::get).toList(); }
    public Collection<DeferredHolder<R,T>> getAllHolders(K1 key1) { return this.getSubmap(key1).values(); }
    public Collection<T> getAll(K1 key1) { return this.getAllHolders(key1).stream().map(DeferredHolder::get).toList(); }

    private List<K1> getKey1Sorted() { return this.getKey1Sorted(this.sorter1); }
    private List<K1> getKey1Sorted(Comparator<K1> sorter) {
        List<K1> keys = new ArrayList<>(this.values.keySet());
        keys.sort(sorter);
        return keys;
    }

    private List<K2> getKey2Sorted(Map<K2,DeferredHolder<R,T>> map) { return this.getKey2Sorted(map,this.sorter2); }
    private List<K2> getKey2Sorted(Map<K2,DeferredHolder<R,T>> map,Comparator<K2> sorter) {
        List<K2> keys = new ArrayList<>(map.keySet());
        keys.sort(sorter);
        return keys;
    }

    public List<DeferredHolder<R,T>> getAllHoldersSorted() { return this.getAllHoldersSorted(this.sorter1,this.sorter2); }
    public List<DeferredHolder<R,T>> getAllHoldersSorted(Predicate<K1> filter1, Predicate<K2> filter2) { return this.getAllHoldersSorted(filter1,filter2,this.sorter1,this.sorter2); }
    public List<DeferredHolder<R,T>> getAllHoldersSorted(Comparator<K1> sorter1,Comparator<K2> sorter2) { return this.getAllHoldersSorted(Predicates.alwaysTrue(),Predicates.alwaysTrue(),sorter1,sorter2); }
    public List<DeferredHolder<R,T>> getAllHoldersSorted(Predicate<K1> filter1,Predicate<K2> filter2,Comparator<K1> sorter1,Comparator<K2> sorter2) {
        List<K1> keys1 = this.getKey1Sorted(sorter1).stream().filter(filter1).toList();
        List<DeferredHolder<R,T>> result = new ArrayList<>();
        for(K1 key1 : keys1)
        {
            Map<K2,DeferredHolder<R,T>> submap = this.getSubmap(key1);
            List<K2> keys2 = this.getKey2Sorted(submap,sorter2).stream().filter(filter2).toList();
            for(K2 key2 : keys2)
                result.add(submap.get(key2));
        }
        return result;
    }

    public List<T> getAllSorted() { return this.getAllSorted(this.sorter1,this.sorter2); }
    public List<T> getAllSorted(Predicate<K1> filter1, Predicate<K2> filter2) { return this.getAllSorted(filter1,filter2,this.sorter1,this.sorter2); }
    public List<T> getAllSorted(Comparator<K1> sorter1,Comparator<K2> sorter2) { return this.getAllSorted(Predicates.alwaysTrue(),Predicates.alwaysTrue(),sorter1,sorter2); }
    public List<T> getAllSorted(Predicate<K1> filter1,Predicate<K2> filter2,Comparator<K1> sorter1,Comparator<K2> sorter2) { return this.getAllHoldersSorted(filter1,filter2,sorter1,sorter2).stream().map(DeferredHolder::get).collect(Collectors.toCollection(ArrayList::new)); }

    public void forEachKey1(Consumer<K1> action) {
        for(K1 key : this.getKey1Sorted())
            action.accept(key);
    }

    public void forEachHolder(Consumer3<K1,K2,DeferredHolder<R,T>> action) {
        for(K1 key1 : this.getKey1Sorted())
        {
            Map<K2,DeferredHolder<R,T>> submap = this.getSubmap(key1);
            for(K2 key2 : this.getKey2Sorted(submap))
                action.accept(key1,key2,submap.get(key2));
        }
    }
    public void forEach(Consumer3<K1,K2,T> action) { this.forEachHolder((k1,k2,holder) -> action.accept(k1,k2,holder.get())); }

    public Supplier<Set<R>> getFutureSet() {
        return () -> {
            Set<R> set = new HashSet<>();
            this.forEach((k1,k2,value) -> set.add(value));
            return set;
        };
    }

}