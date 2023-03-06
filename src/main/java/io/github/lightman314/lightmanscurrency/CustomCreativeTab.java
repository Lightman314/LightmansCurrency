package io.github.lightman314.lightmanscurrency;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;

public class CustomCreativeTab extends ItemGroup {

	private final ItemSorter itemSorter;
	
	Supplier<IItemProvider> iconItem;
	
	public CustomCreativeTab(String label, Supplier<IItemProvider> iconItem)
	{
		super(label);
		this.iconItem = iconItem;
		this.itemSorter = new ItemSorter();
	}
	
	@Override
	public @Nonnull ItemStack makeIcon()
	{
		if(this.iconItem != null)
			return new ItemStack(this.iconItem.get());
		return ItemStack.EMPTY;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillItemList(@Nonnull NonNullList<ItemStack> items) {
		
		super.fillItemList(items);

		// Sort the item list using the ItemSorter instance
		items.sort(itemSorter);
		
	}

	public final ItemSorterBuilder startInit() { return new ItemSorterBuilder(this.itemSorter); }


	public void addToSortingList(List<IItemProvider> extras)
	{
		this.itemSorter.addToSortingList(extras);
	}

	/**
	 * Initializes the sorting list of the item group. Should be called in the FMLCommonSetupEvent.
	 */
	@Deprecated
	public void initSortingList(List<IItemProvider> defaultList)
	{
		this.itemSorter.initSortingList(defaultList);
	}


	/**
	 * Initializes the sorting list of the item group. Should be called in the FMLCommonSetupEvent.
	 */
	@Deprecated
	public void initSortingList2(List<RegistryObject<? extends IItemProvider>> defaultList)
	{
		List<IItemProvider> list = new ArrayList<>();
		for(RegistryObject<? extends IItemProvider> item : defaultList)
		{
			IItemProvider i = item.get();
			if(i != null)
				list.add(i);
		}
		this.itemSorter.initSortingList(list);
	}

	public static final class ItemSorterBuilder {

		private final ItemSorter sorter;
		private final ArrayList<Item> sortList = new ArrayList<>();
		private ItemSorterBuilder(ItemSorter sorter) { this.sorter = sorter; }

		public ItemSorterBuilder add(Object... items) {
			for(Object object : items)
				this.addItem(object);
			return this;
		}

		private void addItem(Object object) {
			if(object instanceof IItemProvider)
			{
				IItemProvider item = (IItemProvider)object;
				this.addItem(item.asItem());
			}
			else if(object instanceof RegistryObject<?>)
			{
				RegistryObject<?> registryObject = (RegistryObject<?>)object;
				this.addItem(registryObject.get());
			}

			else if(object instanceof RegistryObjectBundle<?,?>)
			{
				RegistryObjectBundle<?,?> bundle = (RegistryObjectBundle<?,?>)object;
				for(Object obj : bundle.getAllSorted())
					this.addItem(obj);
			}
			else if(object instanceof RegistryObjectBiBundle<?,?,?>)
			{
				RegistryObjectBiBundle<?,?,?> bundle = (RegistryObjectBiBundle<?,?,?>)object;
				for(Object obj : bundle.getAllSorted())
					this.addItem(obj);
			}
			else if(object instanceof List<?>)
			{
				List<?> list = (List<?>)object;
				for(Object obj : list)
					this.addItem(obj);
			}
			else
				LightmansCurrency.LogWarning("Could not parse '" + object.getClass().getName() + "' as an item for the sorting list.");
		}

		private void addItem(Item item) {
			if(!this.sortList.contains(item))
				this.sortList.add(item);
		}

		public void build() { this.sorter.initSortingList(this); }

	}


	
	private static class ItemSorter implements Comparator<ItemStack>
	{
		
		public ItemSorter() {}
		
		private ArrayList<Item> sortList = null;
		
		private ArrayList<Item> convertList(List<IItemProvider> sourceList)
		{
			ArrayList<Item> list = Lists.newArrayList();
			for (IItemProvider itemLike : sourceList) {
				list.add(itemLike.asItem());
			}
			return list;
		}

		@Deprecated
		public void initSortingList(List<IItemProvider> sortList)
		{
			if(this.sortList == null)
				this.sortList = this.convertList(sortList);
			else
			{
				List<Item> copyList = this.sortList;
				this.sortList = this.convertList(sortList);
				this.sortList.addAll(copyList);
			}
		}

		public void initSortingList(ItemSorterBuilder builder) {
			if(this.sortList == null)
				this.sortList = new ArrayList<>(builder.sortList);
			else
			{
				List<Item> copyList = this.sortList;
				this.sortList = new ArrayList<>(builder.sortList);
				this.sortList.addAll(copyList);
			}
		}
		
		public void addToSortingList(List<IItemProvider> extras)
		{
			if(this.sortList == null)
			{
				//LightmansCurrency.LogWarning("Sorting list has not been initialized. Adding temporarily, until the official init arrives.");
				this.sortList = this.convertList(extras);
				return;
			}
			for (IItemProvider extra : extras) {
				this.sortList.add(extra.asItem());
			}
			LightmansCurrency.LogInfo("Added " + extras.size() + " items to the creative tab sorting list.");
		}
		
		@Override
		public int compare(ItemStack o1, ItemStack o2) {
			
			Item item1 = o1.getItem();
			Item item2 = o2.getItem();
			
			if(this.sortList == null)
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
				return Integer.compare(this.indexOf(item1), this.indexOf(item2));
			}
			
			//No other sort method found, do nothing.
			return 0;
			
		}
		
		private boolean sortListContains(Item item)
		{
			return indexOf(item) >= 0;
		}
		
		private int indexOf(Item item) { return this.sortList.indexOf(item); }
		
	}
	
}
