package io.github.lightman314.lightmanscurrency.common.core.groups;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;

import javax.annotation.Nonnull;

public class RegistryObjectBundle<T,L> {

	private final Comparator<L> sorter;

	private boolean locked = false;
	public RegistryObjectBundle<T,L> lock() { this.locked = true; return this; }

	public RegistryObjectBundle(@Nonnull Comparator<L> sorter) { this.sorter = sorter; }

	private final Map<L,Supplier<T>> values = new HashMap<>();
	
	public void put(L key, Supplier<T> value) {
		if(this.locked)
		{
			LightmansCurrency.LogWarning("Attempted to put an object in the bundle after it's been locked.");
			return;
		}
		if(this.values.containsKey(key))
		{
			LightmansCurrency.LogWarning("Attempted to put a second object with key '" + key.toString() + "' into the registry bundle.");
			return;
		}
		this.values.put(key,value);
	}
	
	public Supplier<T> getRegistryObject(L key) {
		if(this.values.containsKey(key))
			return this.values.get(key);
		return null;
	}
	
	public T get(L key) {
		Supplier<T> obj = this.getRegistryObject(key);
		if(obj != null)
			return obj.get();
		return null;
	}
	
	public Collection<Supplier<T>> getAllRegistryObjects() { return this.values.values(); }
	public List<T> getAll() {
		List<T> values = new ArrayList<>();
		for(Supplier<T> value : this.getAllRegistryObjects())
			values.add(value.get());
		return values;
	}

	@SafeVarargs
	public final List<T> getSome(L... keys) {
		List<T> values = new ArrayList<>();
		for(L key : keys)
			values.add(this.get(key));
		return values;
	}

	private List<L> getKeysSorted() { return this.getKeysSorted(this.sorter); }
	private List<L> getKeysSorted(Comparator<L> sorter) {
		List<L> keys = new ArrayList<>(this.values.keySet());
		keys.sort(sorter);
		return keys;
	}

	public List<T> getAllSorted() { return this.getAllSorted(BundleRequestFilter.ALL); }
	public List<T> getAllSorted(BundleRequestFilter filter) { return this.getAllSorted(filter, this.sorter); }

	public List<T> getAllSorted(Comparator<L> sorter) { return this.getAllSorted(BundleRequestFilter.ALL, sorter); }
	public List<T> getAllSorted(BundleRequestFilter filter, Comparator<L> sorter)
	{
		List<L> keys = this.getKeysSorted(sorter).stream().filter(filter::filterKey).toList();
		List<T> result = new ArrayList<>();
		for(L key : keys)
		{
			result.add(this.get(key));
		}
		return result;
	}

	public List<Supplier<T>> getSupplier() {
		List<Supplier<T>> result = new ArrayList<>();
		for(L key : this.values.keySet())
			result.add(() -> this.get(key));
		return result;
	}

	public void forEach(BiConsumer<L,Supplier<T>> consumer) {
		List<L> keys = this.getKeysSorted(this.sorter);
		for(L key : keys)
			consumer.accept(key, this.values.get(key));
	}
	
}
