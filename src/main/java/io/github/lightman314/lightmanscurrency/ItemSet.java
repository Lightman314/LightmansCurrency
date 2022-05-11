package io.github.lightman314.lightmanscurrency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.Item;

@Deprecated
public class ItemSet<T> implements IItemSet<T>{

	Map<T, Item> set;
	
	public ItemSet() { set = new HashMap<>(); }
	
	
	public Item getItem(T key)
	{
		return null;
	}
	
	public List<Item> getAllItems()
	{
		return null;
	}
	
	public void add(T key, Item item)
	{
		if(set.containsKey(key))
		{
			LightmansCurrency.LogWarning("ItemSet already contains an entry for " + key.toString() + ".");
			return;
		}
		set.put(key, item);
	}
	
}
