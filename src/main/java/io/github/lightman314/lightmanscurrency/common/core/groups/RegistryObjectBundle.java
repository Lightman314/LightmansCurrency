package io.github.lightman314.lightmanscurrency.common.core.groups;

import java.util.*;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.registries.RegistryObject;

public class RegistryObjectBundle<T,L> {

	private final Comparator<L> sorter;

	private boolean locked = false;
	public RegistryObjectBundle<T,L> lock() { this.locked = true; return this; }

	public RegistryObjectBundle(Comparator<L> sorter) { this.sorter = sorter; }

	private final Map<L,RegistryObject<T>> values = new HashMap<>();
	
	public void put(L key, RegistryObject<T> value) {
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
	
	
	public RegistryObject<T> getRegistryObject(L key) {
		if(this.values.containsKey(key))
			return this.values.get(key);
		return null;
	}
	
	public T get(L key) {
		RegistryObject<T> obj = this.getRegistryObject(key);
		if(obj != null)
			return obj.get();
		return null;
	}
	
	public Collection<RegistryObject<T>> getAllRegistryObjects() { return this.values.values(); }
	public List<T> getAll() {
		List<T> values = new ArrayList<>();
		for(RegistryObject<T> value : this.getAllRegistryObjects())
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

	public List<T> getAllSorted() { return this.getAllSorted(this.sorter); }

	public List<T> getAllSorted(Comparator<L> sorter)
	{
		List<L> keys = new ArrayList<>(this.values.keySet().stream().toList());
		keys.sort(sorter);
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
	
}
