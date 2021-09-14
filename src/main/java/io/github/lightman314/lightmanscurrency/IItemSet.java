package io.github.lightman314.lightmanscurrency;

import java.util.List;

import net.minecraft.item.Item;

public interface IItemSet<T> {

	public Item getItem(T key);
	public List<Item> getAllItems();
	
}
