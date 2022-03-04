package io.github.lightman314.lightmanscurrency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockItemSet<T> implements IItemSet<T>{

	Map<T,BlockItemPair> set;
	
	public BlockItemSet() { set = new HashMap<>(); }
	
	public List<BlockItemPair> getAll()
	{
		List<BlockItemPair> values = new ArrayList<>();
		set.forEach((key, blockItemPair) -> values.add(blockItemPair));
		return values;
	}
	
	public List<Block> getAllBlocks()
	{
		List<Block> values = new ArrayList<>();
		set.forEach((key, blockItemPair) -> values.add(blockItemPair.block));
		return values;
	}
	
	public List<Item> getAllItems()
	{
		List<Item> values = new ArrayList<>();
		set.forEach((key, blockItemPair) -> values.add(blockItemPair.item));
		return values;
	}
	
	public BlockItemPair get(T key)
	{
		if(set.containsKey(key))
			return set.get(key);
		return null;
	}
	
	public Block getBlock(T key)
	{
		BlockItemPair pair = get(key);
		if(pair != null)
			return pair.block;
		return Blocks.AIR;
	}
	
	public Item getItem(T key)
	{
		BlockItemPair pair = get(key);
		if(pair != null)
			return pair.item;
		return Items.AIR;
	}
	
	public void add(T key, BlockItemPair block)
	{
		if(set.containsKey(key))
		{
			LightmansCurrency.LogWarning("BlockItemSet already contains an entry for " + key.toString() + ".");
			return;
		}
		set.put(key,  block);
	}
	
	
}
