package io.github.lightman314.lightmanscurrency;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CustomItemGroup extends ItemGroup {

	private final ItemSorter itemSorter;
	
	Supplier<IItemProvider> iconItem;
	
	public CustomItemGroup(String label, Supplier<IItemProvider> iconItem)
	{
		super(label);
		this.iconItem = iconItem;
		this.itemSorter = new ItemSorter();
	}
	
	@Override
	public ItemStack createIcon()
	{
		if(this.iconItem != null)
			return new ItemStack(this.iconItem.get());
		return ItemStack.EMPTY;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void fill(NonNullList<ItemStack> items) {
		
		super.fill(items);

		// Sort the item list using the ItemSorter instance
		Collections.sort(items, itemSorter);
		
	}
	
	public void addToSortingList(List<IItemProvider> extras)
	{
		this.itemSorter.addToSortingList(extras);
	}
	
	/**
	 * Initializes the sorting list of the item group. Should be called in the FMLCommonSetupEvent.
	 */
	public void initSortingList(List<IItemProvider> defaultList)
	{
		this.itemSorter.initSortingList(defaultList);
	}
	
	private static class ItemSorter implements Comparator<ItemStack>
	{
		
		public ItemSorter()
		{
			
		}
		
		private List<IItemProvider> sortList = null;
		public void initSortingList(List<IItemProvider> sortList)
		{
			if(this.sortList == null)
				this.sortList = sortList;
			else
			{
				List<IItemProvider> copyList = this.sortList;
				this.sortList = sortList;
				for(int i = 0; i < copyList.size(); i++)
				{
					this.sortList.add(copyList.get(i));
				}
			}
		}
		
		public void addToSortingList(List<IItemProvider> extras)
		{
			if(this.sortList == null)
			{
				LightmansCurrency.LogWarning("Sorting list has not been initialized. Adding temporarily, until the official init arrives.");
				this.sortList = extras;
				//return;
			}
			for(int i = 0; i < extras.size(); i++)
			{
				this.sortList.add(extras.get(i));
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
			for(int i = 0; i < sortList.size(); i++)
			{
				if(item == sortList.get(i).asItem())
					return i;
			}
			return -1;
		}
		
	}
	
}
