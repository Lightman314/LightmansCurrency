package io.github.lightman314.lightmanscurrency.core.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.registries.RegistryObject;

public class RegistryObjectBundle<T,L> {

	private boolean locked = false;
	public RegistryObjectBundle<T,L> lock() { this.locked = true; return this; }
	
	private final Map<L,RegistryObject<T>> values = new HashMap<>();
	
	public void put(L key, RegistryObject<T> value) {
		if(this.locked)
		{
			LightmansCurrency.LogWarning("Attempted to put an object in the bundle after it's been locked.");
			return;
		}
		if(this.values.containsKey(key))
		{
			LightmansCurrency.LogWarning("Attempted to put a second object with key " + key.toString() + " into the registry bundle.");
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
		if(this.values.containsKey(key))
			return this.values.get(key).get();
		return null;
	}
	
	public Collection<RegistryObject<T>> getAllRegistryObjects() { return this.values.values(); }
	public List<T> getAll() {
		List<T> values = new ArrayList<>();
		for(RegistryObject<T> value : this.getAllRegistryObjects())
			values.add(value.get());
		return values;
	}
	
	public List<Supplier<T>> getSupplier() {
		List<Supplier<T>> result = new ArrayList<>();
		for(L key : this.values.keySet())
			result.add(() -> this.get(key));
		return result;
	}
	
}
