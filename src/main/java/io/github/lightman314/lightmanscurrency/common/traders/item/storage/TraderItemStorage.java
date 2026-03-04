package io.github.lightman314.lightmanscurrency.common.traders.item.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.ICanCopy;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
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

    public static final Codec<TraderItemStorage> CODEC = Codec.withAlternative(CodecHelper.UNLIMITED_ITEM.listOf()
            .xmap(TraderItemStorage::new,s -> s.storage),
            CodecHelper.oldValueListLoader(TraderItemStorage::loadOldData,"Trader Item Storage"));

	private Predicate<ItemStack> filter = i -> false;
	private IntSupplier storageLimit = () -> 0;
    private final List<Runnable> listeners = new ArrayList<>();

	private final List<ItemStack> storage = new ArrayList<>();

    public TraderItemStorage() { }
	public TraderItemStorage(Predicate<ItemStack> filter,IntSupplier storageLimit) {
        this.filter = filter;
        this.storageLimit = storageLimit;
    }
    private TraderItemStorage(List<ItemStack> items) {
        for(ItemStack s : items)
            this.forceAddItem(s);
    }

    public TraderItemStorage withFilter(Predicate<ItemStack> filter) { this.filter = filter; return this;}
    public TraderItemStorage withStorageLimit(IntSupplier storageLimit) { this.storageLimit = storageLimit; return this; }

    public TraderItemStorage withListener(Runnable listener) { this.listeners.add(listener); return this; }

    public Tag save(DataContext<Tag> context) { return CODEC.encodeStart(context.ops(),this).getOrThrow(); }

	public CompoundTag save(CompoundTag compound, String tag, DataContext<Tag> context) {
		compound.put(tag, CODEC.encodeStart(context.ops(),this).getOrThrow());
		return compound;
	}

    @Deprecated
	public void load(CompoundTag compound, String tag, DataContext<Tag> context) {
		if(compound.contains(tag, Tag.TAG_LIST))
            this.load(CODEC.decode(context.ops(),compound.get(tag)).getOrThrow().getFirst());
	}

    public void load(Tag tag,DataContext<Tag> context) { this.load(CODEC.decode(context.ops(),tag).getOrThrow().getFirst()); }

    private void load(TraderItemStorage other) { this.load(other.storage); }

    public void load(List<ItemStack> items) {
        this.storage.clear();
        this.storage.addAll(ItemHandlerUtil.copyList(items));
    }

    @SuppressWarnings("deprecation")
    private static TraderItemStorage loadOldData(ListTag list, HolderLookup.Provider lookup)
    {
        List<ItemStack> storage = new ArrayList<>();
        for(int i = 0; i < list.size(); ++i)
        {
            CompoundTag itemTag = list.getCompound(i);
            ItemStack item = InventoryUtil.loadItemNoLimits(itemTag,lookup);
            if(!item.isEmpty())
                storage.add(item);
        }
        return new TraderItemStorage(storage);
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
			if(ItemStack.isSameItemSameComponents(stack, item))
				return stack.getCount() >= item.getCount();
		}
		return false;
	}
	
	/**
	 * Returns whether the item storage has the given item.
	 */
	public boolean hasItems(ItemStack... items)
	{
		for(ItemStack item : ItemHandlerUtil.combineStacks(ImmutableList.copyOf(items)))
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
        return this.filter.test(item);
	}
	
	/**
	 * Returns the maximum count of the given item that is allowed to be placed in storage.
	 */
	public int getMaxAmount() { return this.storageLimit.getAsInt(); }
	
	/**
	 * Returns the amount of the given item within the storage.
	 */
	public int getItemCount(ItemStack item) {
		for(ItemStack stack : this.storage)
		{
			if(ItemStack.isSameItemSameComponents(item,stack))
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
			if(stack.is(itemTag) && !blacklist.contains(stack.getItem()))
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
	public boolean canFitItems(ItemStack... items) { return this.canFitItems(ImmutableList.copyOf(items)); }

	public boolean canFitItems(List<ItemStack> items) {
		if(items == null)
			return true;
		for(ItemStack item : ItemHandlerUtil.combineStacks(items))
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
			if (ItemStack.isSameItemSameComponents(stack, item)) {
				stack.grow(item.getCount());
                this.setChanged();
				return;
			}
		}
		this.storage.add(item.copy());
        this.setChanged();
	}
	
	/**
	 * Removes the requested item from storage. Limits the amount removed by the stacks maximum stack size.
	 * @return The item that was removed successfully.
	 */
	public ItemStack removeItemLimited(ItemStack item) {
		if(!this.hasItem(item))
			return ItemStack.EMPTY;
		for(int i = 0; i < this.storage.size(); ++i)
		{
			ItemStack stack = this.storage.get(i);
			if(ItemStack.isSameItemSameComponents(item, stack))
			{
				int amountToRemove = Math.min(item.getCount(),item.getMaxStackSize());
				ItemStack output = stack.split(amountToRemove);
				if(stack.isEmpty())
					this.storage.remove(i);
                this.setChanged();
				return output;
			}
		}
		return ItemStack.EMPTY;
	}

    /**
     * Removes the requested item from storage. Does not limit the amount removed by item stack size
     * @return The item that was removed successfully.
     */
    public ItemStack removeItemUnlimited(ItemStack item) {
        for(int i = 0; i < this.storage.size(); ++i)
        {
            ItemStack stack = this.storage.get(i);
            if(ItemStack.isSameItemSameComponents(item,stack))
            {
                ItemStack output = stack.split(item.getCount());
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
		removeItemCount(s -> s.is(itemTag),count,ignoreIfPossible,s -> blacklist.stream().anyMatch(b -> s.getItem() == b));
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
			if(ItemStack.isSameItemSameComponents(item,stack))
				return true;
		}
		return false;
	}
	
	public static class LockedTraderStorage extends TraderItemStorage {

		public LockedTraderStorage(List<ItemStack> startingInventory) { super(startingInventory); }
		@Override
		public boolean allowItem(ItemStack item) { return false; }
        @Override
        public void forceAddItem(ItemStack item) { }
        @Override
        public ItemStack removeItemLimited(ItemStack item) {
            if(!this.hasItem(item))
                return ItemStack.EMPTY;
            return item.copy();
        }
    }
	
	@Override
	public TraderItemStorage copy() {
		TraderItemStorage copy = new TraderItemStorage(this.filter,this.storageLimit);
		for(ItemStack stack : this.storage)
			copy.forceAddItem(stack.copy());
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
			this.removeItemUnlimited(removedStack);
		}
		return removedStack;
	}

	@Override
	public int getSlotLimit(int slot) {
		return this.getMaxAmount();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) { return this.allowItem(stack); }

    private void setChanged()
    {
        for(Runnable l : new ArrayList<>(this.listeners))
            l.run();
    }
	
}
