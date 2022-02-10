package io.github.lightman314.lightmanscurrency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CustomCreativeTab extends CreativeModeTab {

	private final ItemSorter itemSorter;
	
	Supplier<ItemLike> iconItem;
	
	public CustomCreativeTab(String label, Supplier<ItemLike> iconItem)
	{
		super(label);
		this.iconItem = iconItem;
		this.itemSorter = new ItemSorter();
	}
	
	@Override
	public ItemStack makeIcon()
	{
		if(this.iconItem != null)
			return new ItemStack(this.iconItem.get());
		return ItemStack.EMPTY;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillItemList(NonNullList<ItemStack> items) {
		
		super.fillItemList(items);

		// Sort the item list using the ItemSorter instance
		Collections.sort(items, itemSorter);
		
	}
	
	public void addToSortingList(List<ItemLike> extras)
	{
		this.itemSorter.addToSortingList(extras);
	}
	
	/**
	 * Initializes the sorting list of the item group. Should be called in the FMLCommonSetupEvent.
	 */
	public void initSortingList(List<ItemLike> defaultList)
	{
		this.itemSorter.initSortingList(defaultList);
	}
	
	private static class ItemSorter implements Comparator<ItemStack>
	{
		
		public ItemSorter()
		{
			
		}
		
		private ArrayList<Item> sortList = null;
		
		private ArrayList<Item> convertList(List<ItemLike> sourceList)
		{
			ArrayList<Item> list = Lists.newArrayList();
			for(int i = 0; i < sourceList.size(); ++i)
			{
				list.add(sourceList.get(i).asItem());
			}
			return list;
		}
		
		public void initSortingList(List<ItemLike> sortList)
		{
			if(this.sortList == null)
			{
				this.sortList = this.convertList(sortList);
			}
			else
			{
				List<Item> copyList = this.sortList;
				this.sortList = this.convertList(sortList);
				for(int i = 0; i < copyList.size(); i++)
				{
					this.sortList.add(copyList.get(i));
				}
			}
		}
		
		public void addToSortingList(List<ItemLike> extras)
		{
			if(this.sortList == null)
			{
				//LightmansCurrency.LogWarning("Sorting list has not been initialized. Adding temporarily, until the official init arrives.");
				this.sortList = this.convertList(extras);
				return;
			}
			for(int i = 0; i < extras.size(); i++)
			{
				this.sortList.add(extras.get(i).asItem());
			}
			LightmansCurrency.LogInfo("Added " + extras.size() + " items to the creative tab sorting list.");
		}
		
		@Override
		public int compare(ItemStack o1, ItemStack o2) {
			
			Item item1 = o1.getItem();
			Item item2 = o2.getItem();
			
			if(sortList == null)
			{
				LightmansCurrency.LogWarning("No sortlist defined for this CurrencyGroup.");
				return 0;
			}
			
			//If item1 is on the sort list and item2 isn't, sort item1 before item2
			if(sortList.contains(item1) && !sortList.contains(item2))
			{
				return -1;
			}
			
			//If item2 is on the sort list and item1 isn't, sort item1 before item2
			if(!sortList.contains(item1) && sortList.contains(item2))
			{
				return 1;
			}
			
			//If both items are on the sort list, sort by index
			if(sortListContains(item1) && sortListContains(item2))
			{
				int index1 = indexOf(item1);
				int index2 = indexOf(item2);
				//CurrencyMod.LOGGER.info("Sorting items at index " + index1 + " & " + index2);
				if(index1 < index2)
					return -1;
				if(index1 > index2)
					return 1;
				return 0;
			}
			
			//No other sort method found, do nothing.
			return 0;
			
		}
		
		private boolean sortListContains(Item item)
		{
			return indexOf(item) >= 0;
		}
		
		private int indexOf(Item item)
		{
			for(int i = 0; i < this.sortList.size(); i++)
			{
				if(item == this.sortList.get(i))
					return i;
			}
			return -1;
		}
		
	}
	
}
