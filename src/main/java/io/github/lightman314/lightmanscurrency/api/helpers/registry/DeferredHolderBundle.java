package io.github.lightman314.lightmanscurrency.api.helpers.registry;

import com.google.common.base.Predicates;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class DeferredHolderBundle<K,R,T extends R> {

    private final Comparator<K> sorter;

    private boolean locked = false;
    public DeferredHolderBundle<K,R,T> lock() { this.locked = true; return this; }

    public DeferredHolderBundle(Comparator<K> sorter) { this.sorter = sorter; }

    private final Map<K,DeferredHolder<R,T>> values = new HashMap<>();

    public void put(K key,DeferredHolder<R,T> holder)
    {
        if(this.locked)
            throw new IllegalStateException("Cannot add new values to the bundle after it's been locked!");
        if(this.values.containsKey(key))
            throw new IllegalStateException("Attempted to add an entry of key " + key + " when an entry is already present in the bundle!");
        this.values.put(key,holder);
    }

    public DeferredHolder<R,T> getHolder(K key) { return this.values.get(key); }
    public T get(K key) { return this.getHolder(key).get(); }

    public Collection<DeferredHolder<R,T>> getAllHolders() { return this.values.values(); }
    public Collection<T> getAll() { return this.values.values().stream().map(DeferredHolder::get).toList(); }

    @SafeVarargs
    public final List<DeferredHolder<R,T>> getSomeHolders(K... keys)
    {
        List<DeferredHolder<R,T>> list = new ArrayList<>();
        for(K key : keys)
            list.add(this.getHolder(key));
        return list;
    }
    @SafeVarargs
    public final List<T> getSome(K... keys)
    {
        List<T> list = new ArrayList<>();
        for(K key : keys)
            list.add(this.get(key));
        return list;
    }

    private List<K> getKeysSorted() { return this.getKeysSorted(this.sorter); }
    private List<K> getKeysSorted(Comparator<K> sorter) {
        List<K> keys = new ArrayList<>(this.values.keySet());
        keys.sort(sorter);
        return keys;
    }

    public List<DeferredHolder<R,T>> getAllHoldersSorted() { return this.getAllHoldersSorted(this.sorter); }
    public List<DeferredHolder<R,T>> getAllHoldersSorted(Predicate<K> filter) { return this.getAllHoldersSorted(filter,this.sorter); }
    public List<DeferredHolder<R,T>> getAllHoldersSorted(Comparator<K> sorter) { return this.getAllHoldersSorted(Predicates.alwaysTrue(),sorter); }
    public List<DeferredHolder<R,T>> getAllHoldersSorted(Predicate<K> filter, Comparator<K> sorter) {
        List<K> keys = this.getKeysSorted(sorter).stream().filter(filter).toList();
        List<DeferredHolder<R,T>> result = new ArrayList<>();
        for(K key : keys)
            result.add(this.getHolder(key));
        return result;
    }

    public List<T> getAllSorted() { return this.getAllSorted(this.sorter); }
    public List<T> getAllSorted(Predicate<K> filter) { return this.getAllSorted(filter,this.sorter); }
    public List<T> getAllSorted(Comparator<K> sorter) { return this.getAllSorted(Predicates.alwaysTrue(),sorter); }
    public List<T> getAllSorted(Predicate<K> filter, Comparator<K> sorter) {
        List<K> keys = this.getKeysSorted(sorter).stream().filter(filter).toList();
        List<T> result = new ArrayList<>();
        for(K key : keys)
            result.add(this.get(key));
        return result;
    }

    public void forEachHolder(BiConsumer<K,DeferredHolder<R,T>> action)
    {
        for(K key : this.getKeysSorted())
            action.accept(key,this.getHolder(key));
    }
    public void forEach(BiConsumer<K,T> action) { this.forEachHolder((k,holder) -> action.accept(k,holder.get())); }

    public Supplier<Set<R>> getFutureSet() {
        return () -> {
            Set<R> set = new HashSet<>();
            this.forEach((key,value) -> set.add(value));
            return set;
        };
    }

}