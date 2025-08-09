package io.github.lightman314.lightmanscurrency.common.traders.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.blockentity.handler.ICanCopy;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Range;

public class TraderItemStorage implements IItemHandler, ICanCopy<TraderItemStorage>{

	private final ITraderItemFilter filter;
	private final List<ItemStack> storage = new ArrayList<>();
	
	public TraderItemStorage(@Nonnull ITraderItemFilter filter) { this.filter = filter; }
	
	public CompoundTag save(@Nonnull CompoundTag compound, @Nonnull String tag, @Nonnull HolderLookup.Provider lookup) {
		ListTag list = new ListTag();
		for (ItemStack item : this.storage) {
			if (!item.isEmpty()) {
				list.add(InventoryUtil.saveItemNoLimits(item,lookup));
			}
		}
		compound.put(tag, list);
		return compound;
	}
	
	public void load(@Nonnull CompoundTag compound, @Nonnull String tag, @Nonnull HolderLookup.Provider lookup) {
		if(compound.contains(tag, Tag.TAG_LIST))
		{
			ListTag list = compound.getList(tag, Tag.TAG_COMPOUND);
			this.storage.clear();
			for(int i = 0; i < list.size(); ++i)
			{
				CompoundTag itemTag = list.getCompound(i);
				ItemStack item = InventoryUtil.loadItemNoLimits(itemTag,lookup);
				if(!item.isEmpty())
					this.storage.add(item);
			}
		}
	}
	
	public List<ItemStack> getContents() { return this.storage; }
	
	public List<ItemStack> getSplitContents() {
		List<ItemStack> contents = new ArrayList<>();
		for(ItemStack s : this.storage)
		{
			//Interact with a copy to preserve the original storage data
			ItemStack stack = s.copy();
			int maxCount = stack.getMaxStackSize();
			while(stack.getCount() > maxCount)
				contents.add(stack.split(maxCount));
			contents.add(stack);
		}
		return contents;
	}
	
	public int getSlotCount() { return this.storage.size(); }
	
	/**
	 * Returns whether the item storage has the given item.
	 */
	public boolean hasItem(ItemStack item) {
		for(ItemStack stack : this.storage)
		{
			if(InventoryUtil.ItemMatches(stack, item))
			{
				return stack.getCount() >= item.getCount();
			}
		}
		return false;
	}
	
	/**
	 * Returns whether the item storage has the given item.
	 */
	public boolean hasItems(ItemStack... items)
	{
		for(ItemStack item : InventoryUtil.combineQueryItems(items))
		{
			if(!this.hasItem(item))
				return false;
		}
		return true;
	}
	
	/**
	 * Returns whether the item storage is allowed to be given this item.
	 */
	public boolean allowItem(ItemStack item) {
		if(item.isEmpty())
			return false;
		return this.filter.isItemRelevant(item);
	}
	
	/**
	 * Returns the maximum count of the given item that is allowed to be placed in storage.
	 */
	public int getMaxAmount() {
		return this.filter.getStorageStackLimit();
	}
	
	/**
	 * Returns the amount of the given item within the storage.
	 */
	public int getItemCount(ItemStack item) {
		for(ItemStack stack : this.storage)
		{
			if(InventoryUtil.ItemMatches(item, stack))
				return stack.getCount();
		}
		return 0;
	}

	/**
	 * Returns the amount of the given item within the storage.
	 */
	public int getItemCount(Predicate<ItemStack> filter) {
		int count = 0;
		for(ItemStack stack : this.storage)
		{
			if(filter.test(stack))
				count += stack.getCount();
		}
		return count;
	}
	
	/**
	 * Returns the amount of the given items containing the given item tag within the storage.
	 * Ignores any items listed on the given blacklist.
	 */
	public int getItemTagCount(TagKey<Item> itemTag, Item... blacklistItems) {
		List<Item> blacklist = Lists.newArrayList(blacklistItems);
		int count = 0;
		for(ItemStack stack : this.storage)
		{
			if(InventoryUtil.ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()))
				count += stack.getCount();
		}
		return count;
	}

	@Range(to = 0, from = Integer.MAX_VALUE)
	public int getFittableAmount(ItemStack item) {
		if(!this.allowItem(item))
			return 0;
		return Math.max(0,this.getMaxAmount() - this.getItemCount(item));
	}
	
	/**
	 * Returns the amount of the given item that this storage can fit.
	 */
	public boolean canFitItem(ItemStack item) {
		return this.getFittableAmount(item) >= item.getCount();
	}
	
	/**
	 * Returns the amount of the given item that this storage can fit.
	 */
	public boolean canFitItems(ItemStack... items) {
		for(ItemStack item : InventoryUtil.combineQueryItems(items))
		{
			if(!this.canFitItem(item))
				return false;
		}
		return true;
	}

	public boolean canFitItems(List<ItemStack> items) {
		if(items == null)
			return true;
		for(ItemStack item : InventoryUtil.combineQueryItems(items))
		{
			if(!this.canFitItem(item))
				return false;
		}
		return true;
	}
	
	/**
	 * Attempts to add the entire item stack to storage.
	 * @return Whether the item was added. If false, no partial stack was added to storage.
	 */
	public boolean addItem(ItemStack item) {
		if(!this.canFitItem(item))
			return false;
		this.forceAddItem(item);
		return true;
	}
	
	/**
	 * Attempts to add as much of the item stack to storage as possible.
	 * The input item stack will be shrunk based on the amount that is added.
	 * Use this for player interactions where they attempt to place an item in storage.
	 */
	public void tryAddItem(ItemStack item) {
		if(!this.allowItem(item))
			return;
		int amountToAdd = Math.min(item.getCount(), this.getFittableAmount(item));
		if(amountToAdd > 0)
		{
			ItemStack addStack = item.split(amountToAdd);
			this.forceAddItem(addStack);
		}
	}
	
	/**
	 * Adds the item without performing any checks on maximum quantity or trade verification.
	 * Used to add item to storage from older systems.
	 */
	public void forceAddItem(ItemStack item) {
		if(item.isEmpty())
			return;
		for (ItemStack stack : this.storage) {
			if (InventoryUtil.ItemMatches(stack, item)) {
				stack.grow(item.getCount());
				return;
			}
		}
		this.storage.add(item.copy());
	}
	
	/**
	 * Removes the requested item from storage. Limits the amount removed by the stacks maximum stack size.
	 * @return The item that was removed successfully.
	 */
	public ItemStack removeItem(ItemStack item) {
		if(!this.hasItem(item))
			return ItemStack.EMPTY;
		for(int i = 0; i < this.storage.size(); ++i)
		{
			ItemStack stack = this.storage.get(i);
			if(InventoryUtil.ItemMatches(item, stack))
			{
				int amountToRemove = Math.min(item.getCount(), item.getMaxStackSize());
				ItemStack output = stack.split(amountToRemove);
				if(stack.isEmpty())
					this.storage.remove(i);
				return output;
			}
		}
		return ItemStack.EMPTY;
	}

	/**
	 * Removes the requested amount of items with the given item tag from storage.
	 * Ignores items within the given blacklist.
	 */
	public void removeItemTagCount(TagKey<Item> itemTag, int count, List<ItemStack> ignoreIfPossible, Item... blacklistItems) {
		List<Item> blacklist = Lists.newArrayList(blacklistItems);
		removeItemCount(s -> InventoryUtil.ItemHasTag(s,itemTag),count,ignoreIfPossible,s -> blacklist.stream().anyMatch(b -> s.getItem() == b));
	}

	public void removeItemCount(Predicate<ItemStack> filter, int count, List<ItemStack> ignoreIfPossible, Predicate<ItemStack> blacklist)
	{
		//First pass, honoring the "ignoreIfPossible" list
		for(int i = 0; i < this.storage.size() && count > 0; ++i)
		{
			ItemStack stack = this.storage.get(i);
			if(filter.test(stack) && !blacklist.test(stack) && !ListContains(ignoreIfPossible, stack))
			{
				int amountToTake = Math.min(count, stack.getCount());
				count-= amountToTake;
				stack.shrink(amountToTake);
				if(stack.isEmpty())
				{
					this.storage.remove(i);
					i--;
				}
			}
		}
		//Second pass, ignoring the "ignoreIfPossible" list
		for(int i = 0; i < this.storage.size() && count > 0; ++i)
		{
			ItemStack stack = this.storage.get(i);
			if(filter.test(stack) && !blacklist.test(stack))
			{
				int amountToTake = Math.min(count, stack.getCount());
				count-= amountToTake;
				stack.shrink(amountToTake);
				if(stack.isEmpty())
				{
					this.storage.remove(i);
					i--;
				}
			}
		}
	}
	
	private static boolean ListContains(List<ItemStack> list, ItemStack stack) {
		for(ItemStack item : list) 
		{
			if(InventoryUtil.ItemMatches(item, stack))
				return true;
		}
		return false;
	}
	
	public static class LockedTraderStorage extends TraderItemStorage {

		public LockedTraderStorage(ITraderItemFilter filter, List<ItemStack> startingInventory)
		{
			super(filter);
			for(ItemStack item : startingInventory)
				this.forceAddItem(item);
		}
		
		@Override
		public boolean allowItem(ItemStack item) { return false; }
		
	}
	
	public interface ITraderItemFilter
	{
		boolean isItemRelevant(ItemStack item);
		int getStorageStackLimit();
	}

	
	@Override
	public TraderItemStorage copy() {
		TraderItemStorage copy = new TraderItemStorage(this.filter);
		for(ItemStack stack : this.storage)
			copy.forceAddItem(stack);
		return copy;
	}

	@Override
	public int getSlots() {
		return this.storage.size() + 1;
	}

	@Override
	public @Nonnull ItemStack getStackInSlot(int slot) {
		if(slot >= 0 && slot < this.storage.size())
			return this.storage.get(slot);
		return ItemStack.EMPTY;
	}

	@Override
	public @Nonnull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		int amountToAdd = Math.min(stack.getCount(), this.getFittableAmount(stack));
		//Don't bother doing math if nothing should be added
		if(amountToAdd <= 0)
			return stack.copy();
		ItemStack remainder = stack.copy();
		if(amountToAdd >= stack.getCount())
			remainder = ItemStack.EMPTY;
		else
			remainder.shrink(amountToAdd);
		if(!simulate && amountToAdd > 0)
		{
			ItemStack addedStack = stack.copy();
			addedStack.setCount(amountToAdd);
			//Place the item in storage
			this.forceAddItem(addedStack);
		}
		return remainder;
	}

	@Override
	public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack stackInSlot = this.getStackInSlot(slot);
		int amountToRemove = Math.min(amount, stackInSlot.getCount());
		ItemStack removedStack = stackInSlot.copy();
		if(amountToRemove > 0)
			removedStack.setCount(amountToRemove);
		else
			removedStack = ItemStack.EMPTY;
		if(!simulate && amountToRemove > 0)
		{
			this.removeItem(removedStack);
		}
		return removedStack;
	}

	@Override
	public int getSlotLimit(int slot) {
		return this.getMaxAmount();
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return this.allowItem(stack); }
	
}
