package io.github.lightman314.lightmanscurrency.menus.containers;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class LockableContainer implements IItemHandler{

	private final SimpleContainer container;
	public SimpleContainer getContainer() { return this.container; }
	private final NonNullList<LockData> lock;
	
	private boolean honorFullLocks = true;
	public boolean shouldHonorFullLocks() { return this.honorFullLocks; }
	public void dontHonorFullLocks() { this.honorFullLocks = false; }
	public void honorFullLocks() { this.honorFullLocks = true; }
	
	private final List<IMarkDirty> listeners = new ArrayList<>();
	
	private IExternalInputOutputRules extraRules = null;
	public void setAdditionalRules(IExternalInputOutputRules extraRules) { this.extraRules = extraRules; }
	
	public LockableContainer(int size, IMarkDirty... listeners)
	{
		this.container = new SimpleContainer(size);
		this.lock = NonNullList.withSize(size, new LockData());
		for(int i = 0; i < this.lock.size(); ++i)
			this.lock.set(i, new LockData());
		for(IMarkDirty listener : listeners) this.addListener(listener);
	}
	
	public CompoundTag save(CompoundTag compound) {
		InventoryUtil.saveAllItems("Items", compound, this.container);
		ListTag lockList = new ListTag();
		for(int i = 0; i < this.lock.size(); ++i)
			lockList.add(this.lock.get(i).save());
		compound.put("Lock", lockList);
		return compound;
	}
	
	public LockableContainer(int size, CompoundTag compound, IMarkDirty... listeners)
	{
		//Load items
		if(compound.contains("Items", Tag.TAG_LIST))
			this.container = InventoryUtil.loadAllItems("Items", compound, size);
		else
			this.container = new SimpleContainer(size);
		//Load lock data
		this.lock = NonNullList.withSize(size, new LockData());
		ListTag lockList = compound.contains("Lock", Tag.TAG_LIST) ? compound.getList("Lock", Tag.TAG_COMPOUND) : new ListTag();
		for(int i = 0; i < this.lock.size(); ++i)
		{
			this.lock.set(i, new LockData());
			if(i < lockList.size())
				this.lock.get(i).load(lockList.getCompound(i));
		}
		
		for(IMarkDirty listener : listeners) this.addListener(listener);
	}
	
	public void addListener(IMarkDirty listener) {
		if(!this.listeners.contains(listener))
			this.listeners.add(listener);
	}
	
	public void removeListener(IMarkDirty listener) {
		if(this.listeners.contains(listener))
			this.listeners.remove(listener);
	}
	
	public LockData getLockData(int index) {
		if(index >= 0 && index < this.lock.size())
			return this.lock.get(index);
		return null;
	}
	
	public void setLockData(int index, LockData lockEntry) {
		if(index >= 0 && index < this.lock.size())
		{
			this.lock.get(index).removeListener(this::setChanged);
			this.lock.set(index, lockEntry);
			lockEntry.addListener(this::setChanged);
		}
	}

	public void setChanged() {
		for(int i = 0; i < this.listeners.size(); ++i)
		{
			int oldSize = this.listeners.size();
			this.listeners.get(i).markDirty();
			if(this.listeners.size() < oldSize)
				i--;
		}
	}
	
	public static LockData freshLock() { return new LockData(); }
	
	public static class LockData
	{
		
		private LockData() { }
		
		private List<IMarkDirty> listeners = new ArrayList<>();
		
		public void addListener(IMarkDirty listener) {
			if(!this.listeners.contains(listener))
				this.listeners.add(listener);
		}
		
		public void removeListener(IMarkDirty listener) {
			if(this.listeners.contains(listener))
				this.listeners.remove(listener);
		}
		
		private boolean fullLock = false;
		/**
		 * Whether this slot is locked from all forms of item input.
		 */
		public boolean fullyLocked() { return this.fullLock; }
		/**
		 * Sets whether this slot will block all forms of item input.
		 */
		public void setFullyLocked(boolean fullyLocked) { this.fullLock = fullyLocked; }
		
		private ItemStack itemFilter = ItemStack.EMPTY;
		/**
		 * Whether this slot only accepts a specific item.
		 */
		public boolean hasItemFilter() { return !this.itemFilter.isEmpty(); }
		/**
		 * The item that this slot will accept if an item filter is defined.
		 */
		public ItemStack filterItem() { return this.itemFilter; }
		/**
		 * Sets the item filter.
		 * Set it to an empty stack to clear the filter.
		 */
		public void setFilter(ItemStack item) { this.itemFilter = item.copy(); if(!this.itemFilter.isEmpty()) this.itemFilter.setCount(1); }
		
		/**
		 * Whether the given item is allowed to be placed in this slot.
		 */
		public boolean allow(ItemStack item, boolean honorFullLocks) {
			if(this.fullLock && honorFullLocks)
				return false;
			if(this.hasItemFilter())
				return InventoryUtil.ItemMatches(item, this.itemFilter);
			return true;
		}
		
		private CompoundTag save() {
			CompoundTag compound = new CompoundTag();
			compound.putBoolean("FullLock", this.fullLock);
			if(this.hasItemFilter())
				compound.put("Filter", this.itemFilter.save(new CompoundTag()));
			return compound;
		}
		
		private void load(CompoundTag compound) {
			if(compound.contains("FullLock"))
				this.fullLock = compound.getBoolean("FullLock");
			else
				this.fullLock = false;
			
			if(compound.contains("Filter", Tag.TAG_COMPOUND))
				this.itemFilter = ItemStack.of(compound.getCompound("Filter"));
			else
				this.itemFilter = ItemStack.EMPTY;
		}
		
		public LockData copy() {
			LockData copy = new LockData();
			copy.fullLock = this.fullLock;
			copy.itemFilter = this.itemFilter.copy();
			return copy;
		}
		
		public void setChanged() {
			for(int i = 0; i < this.listeners.size(); ++i)
			{
				int oldSize = this.listeners.size();
				this.listeners.get(i).markDirty();
				if(this.listeners.size() < oldSize)
					i--;
			}
		}
		
	}
	
	public interface IMarkDirty {
		public void markDirty();
	}
	
	public interface IExternalInputOutputRules {
		public boolean allowInput(int slot, LockData lock, ItemStack inputStack);
		public boolean allowOutput(int slot, LockData lock, ItemStack inputStack);
	}

	
	@Override
	public int getSlots() {
		return this.container.getContainerSize();
	}
	

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.container.getItem(slot);
	}
	

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if(this.extraRules != null && !this.extraRules.allowInput(slot, this.getLockData(slot), stack))
			return stack.copy();
		if(this.getLockData(slot).allow(stack, this.honorFullLocks))
		{
			ItemStack returnStack = stack.copy();
			ItemStack currentStack = this.container.getItem(slot);
			if(currentStack.isEmpty())
			{
				if(!simulate)
					this.container.setItem(slot, returnStack);
				returnStack = ItemStack.EMPTY;
			}
			else if(InventoryUtil.ItemMatches(returnStack, currentStack))
			{
				int growAmount = Math.min(stack.getCount(), currentStack.getMaxStackSize() - currentStack.getCount());
				if(growAmount > 0)
				{
					if(!simulate)
						currentStack.grow(growAmount);
					returnStack.shrink(growAmount);
				}
			}
			return returnStack;
		}
		return stack.copy();
	}
	

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if(this.extraRules != null && !this.extraRules.allowOutput(slot, this.getLockData(slot), this.container.getItem(slot)))
			return ItemStack.EMPTY;
		//Always allow extraction
		ItemStack currentStack = this.container.getItem(slot);
		int removeAmount = Math.min(currentStack.getCount(), amount);
		if(!simulate)
		{
			if(removeAmount == currentStack.getCount())
				this.container.setItem(slot, ItemStack.EMPTY);
			else
				currentStack.shrink(removeAmount);
		}
		ItemStack returnStack = currentStack.copy();
		returnStack.setCount(removeAmount);
		return returnStack;
	}
	

	@Override
	public int getSlotLimit(int slot) {
		return this.container.getMaxStackSize();
	}
	

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return this.getLockData(slot).allow(stack, this.honorFullLocks) && (this.extraRules == null || this.extraRules.allowInput(slot, this.getLockData(slot), stack));
	}

}
