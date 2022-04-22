package io.github.lightman314.lightmanscurrency.trader.common;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.blockentity.handler.ICanCopy;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class TraderItemStorage implements IItemHandler, ICanCopy<TraderItemStorage>{

	private final ITraderItemFilter filter;
	private List<ItemStack> storage = new ArrayList<>();
	
	public TraderItemStorage(@Nonnull ITraderItemFilter filter) { this.filter = filter; }
	
	public CompoundTag save(CompoundTag compound, String tag) {
		ListTag list = new ListTag();
		for(int i = 0; i < this.storage.size(); ++i)
		{
			ItemStack item = this.storage.get(i);
			if(!item.isEmpty())
			{
				CompoundTag itemTag = new CompoundTag();
				item.save(itemTag);
				itemTag.putInt("Count", item.getCount());
				list.add(itemTag);
			}
		}
		compound.put(tag, list);
		return compound;
	}
	
	public void load(CompoundTag compound, String tag) {
		if(compound.contains(tag, Tag.TAG_LIST))
		{
			ListTag list = compound.getList(tag, Tag.TAG_COMPOUND);
			this.storage.clear();
			for(int i = 0; i < list.size(); ++i)
			{
				CompoundTag itemTag = list.getCompound(i);
				ItemStack item = ItemStack.of(itemTag);
				item.setCount(itemTag.getInt("Count"));
				if(!item.isEmpty())
					this.storage.add(item);
			}
		}
	}
	
	public void loadFromContainer(Container container) {
		this.storage.clear();
		for(int i = 0; i < container.getContainerSize(); ++i)
			this.forceAddItem(container.getItem(i));
	}
	
	public List<ItemStack> getContents() { return this.storage; }
	
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
	 * @param item
	 * @return
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
	 * Returns the amount of the given items containing the given item tag within the storage.
	 * Ignores any items listed on the given blacklist.
	 */
	public int getItemTagCount(ResourceLocation itemTag, Item... blacklistItems) {
		
		List<Item> blacklist = Lists.newArrayList(blacklistItems);
		int count = 0;
		for(ItemStack stack : this.storage)
		{
			if(InventoryUtil.ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()))
    			count += stack.getCount();
		}
		return count;
	}
	
	public int getFittableAmount(ItemStack item) {
		if(!this.allowItem(item))
			return 0;
		return this.getMaxAmount() - this.getItemCount(item);
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
	 * @param item
	 */
	public void forceAddItem(ItemStack item) {
		if(item.isEmpty())
			return;
		for(int i = 0; i < this.storage.size(); ++i)
		{
			ItemStack stack = this.storage.get(i);
			if(InventoryUtil.ItemMatches(stack, item))
			{
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
	 * @return Whether the items were removed successfully.
	 */
	public void removeItemTagCount(ResourceLocation itemTag, int count, List<ItemStack> ignoreIfPossible, Item... blacklistItems) {
		List<Item> blacklist = Lists.newArrayList(blacklistItems);
		//First pass, honoring the "ignoreIfPossible" list
		for(int i = 0; i < this.storage.size() && count > 0; ++i)
		{
			ItemStack stack = this.storage.get(i);
    		if(InventoryUtil.ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()) && !ListContains(ignoreIfPossible, stack))
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
    		if(InventoryUtil.ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()))
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

		public LockedTraderStorage(ITraderItemFilter  filter) { super(filter); }
		
		@Override
		public boolean allowItem(ItemStack item) { return false; }
		
	}
	
	public interface ITraderItemFilter
	{
		public boolean isItemRelevant(ItemStack item);
		public int getStorageStackLimit();
	}

	
	@Override
	public TraderItemStorage copy() {
		CompoundTag tag = this.save(new CompoundTag(), "copy");
		TraderItemStorage copy = new TraderItemStorage(this.filter);
		copy.load(tag, "copy");
		return copy;
	}

	@Override
	public int getSlots() {
		return this.storage.size() + 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if(slot >= 0 && slot < this.storage.size())
			return this.storage.get(slot);
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		int amountToAdd = Math.min(stack.getCount(), this.getFittableAmount(stack));
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
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
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
	public boolean isItemValid(int slot, ItemStack stack) {
		return this.allowItem(stack);
	}
	
}
