package io.github.lightman314.lightmanscurrency.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class InventoryUtil {

	
	public static SimpleContainer buildInventory(List<ItemStack> list)
	{
		SimpleContainer inventory = new SimpleContainer(list.size());
		for(int i = 0; i < list.size(); i++)
		{
			inventory.setItem(i, list.get(i).copy());
		}
		return inventory;
	}
	
	public static Container buildInventory(ItemStack stack)
	{
		Container inventory = new SimpleContainer(1);
		inventory.setItem(0, stack);
		return inventory;
	}
	
	public static Container copyInventory(Container inventory)
	{
		Container copy = new SimpleContainer(inventory.getContainerSize());
		for(int i = 0; i < inventory.getContainerSize(); i++)
			copy.setItem(i, inventory.getItem(i).copy());
		return copy;
	}
	
	public static NonNullList<ItemStack> buildList(Container inventory)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
		for(int i = 0; i < inventory.getContainerSize(); i++)
		{
			list.set(i, inventory.getItem(i).copy());
		}
		return list;
	}
	
	public static List<ItemStack> copyList(List<ItemStack> list) {
		List<ItemStack> result = new ArrayList<>();
		for(ItemStack stack : list)
			result.add(stack.copy());
		return result;
	}
	
	/**
     * Gets the quantity of a specific item in the given inventory
     * Ignores NBT data, as none is given
     */
    public static int GetItemCount(Container inventory, Item item)
    {
    	int count = 0;
    	for(int i = 0; i < inventory.getContainerSize(); i++)
    	{
    		ItemStack stack = inventory.getItem(i);
    		if(stack.getItem() == item)
    		{
    			count += stack.getCount();
    		}
    	}
    	return count;
    }
    
    /**
     * Gets the quantity of a specific item in the given inventory validating NBT data where applicable
     */
    public static int GetItemCount(Container inventory, ItemStack item)
    {
    	int count = 0;
    	for(int i = 0; i < inventory.getContainerSize(); i++)
    	{
    		ItemStack stack = inventory.getItem(i);
    		if(ItemMatches(stack, item))
    			count += stack.getCount();
    	}
    	return count;
    }

	/**
	 * Gets the quantity of a specific item in the given inventory validating NBT data where applicable
	 */
	public static int GetItemCount(IItemHandler inventory, ItemStack item)
	{
		int count = 0;
		for(int i = 0; i < inventory.getSlots(); i++)
		{
			ItemStack stack = inventory.getStackInSlot(i);
			if(ItemMatches(stack, item))
				count += stack.getCount();
		}
		return count;
	}

	/**
	 * Gets the quantity of a matching item in the given inventory
	 */
	public static int GetItemCount(Container inventory, Predicate<ItemStack> filter)
	{
		int count = 0;
		for(int i = 0; i < inventory.getContainerSize(); ++i)
		{
			ItemStack stack = inventory.getItem(i);
			if(filter.test(stack))
				count += stack.getCount();
		}
		return count;
	}
    
    /**
     * Removes the quantity of a specific item in the given inventory
     * Ignores NBT data as none is given
     * @return Whether the full amount of items were successfully taken.
     */
    public static boolean RemoveItemCount(Container inventory, Item item, int count)
    {
    	for(int i = 0; i < inventory.getContainerSize(); i++)
    	{
    		ItemStack stack = inventory.getItem(i);
    		if(stack.getItem() == item)
    		{
    			if(stack.getCount() > count)
    			{
    				stack.shrink(count);
    				return true;
    			}
    			else
    			{
    				count -= stack.getCount();
    				inventory.setItem(i, ItemStack.EMPTY);
    			}
    		}
    	}
    	return count <= 0;
    }
	
    /**
     * Removes the given item stack from the given inventory, validating nbt data.
     * @return Whether the full amount of items were successfully taken.
     */
    public static boolean RemoveItemCount(Container inventory, ItemStack item)
    {
    	if(GetItemCount(inventory, item) < item.getCount())
    		return false;
    	int count = item.getCount();
    	for(int i = 0; i < inventory.getContainerSize(); i++)
    	{
    		ItemStack stack = inventory.getItem(i);
    		if(ItemMatches(stack, item))
    		{
    			int amountToTake = MathUtil.clamp(count, 0, stack.getCount());
    			count -= amountToTake;
    			if(amountToTake == stack.getCount())
    				inventory.setItem(i, ItemStack.EMPTY);
    			else
    				stack.shrink(amountToTake);
    		}
    	}
    	return true;
    }
    
    /**
     * Removes the given item stack from the given inventory, validating nbt data.
     * @return Whether the full amount of items were successfully taken.
     */
    public static boolean RemoveItemCount(IItemHandler itemHandler, ItemStack item)
    {
    	if(!CanExtractItem(itemHandler, item))
    		return false;
    	int amountToRemove = item.getCount();
    	for(int i = 0; i < itemHandler.getSlots() && amountToRemove > 0; i++)
    	{
    		ItemStack stack = itemHandler.getStackInSlot(i);
    		if(ItemMatches(stack, item))
    		{
    			ItemStack removedStack = itemHandler.extractItem(i, amountToRemove, false);
    			if(ItemMatches(removedStack, item))
    				amountToRemove -= removedStack.getCount();
    			else //Put the item back
    				itemHandler.insertItem(i, removedStack, false);
    		}
    	}
    	return true;
    }
    
    /**
     * Returns whether the given item stack can be successfully removed from the item handler.
     */
    public static boolean CanExtractItem(IItemHandler itemHandler, ItemStack item) {
    	int amountToRemove = item.getCount();
    	for(int i = 0; i < itemHandler.getSlots() && amountToRemove > 0; ++i)
    	{
    		if(ItemMatches(itemHandler.getStackInSlot(i), item))
    		{
    			ItemStack removedStack = itemHandler.extractItem(i, amountToRemove, true);
    			if(ItemMatches(removedStack, item))
    				amountToRemove -= removedStack.getCount();
    		}
    	}
    	return amountToRemove == 0;
    }
    
    /**
     * Returns the number of the given item stack that will fit in the container.
     */
    public static int GetItemSpace(Container container, ItemStack item)
    {
    	return GetItemSpace(container, item, 0, container.getContainerSize());
    }
    
    /**
     * Returns the number of the given item stack that will fit into the given portion of the container.
     */
    public static int GetItemSpace(Container container, ItemStack item, int startingIndex, int stopIndex)
    {
    	int count = 0;
    	for(int i = startingIndex; i < stopIndex && i < container.getContainerSize(); ++i)
    	{
    		ItemStack stack = container.getItem(i);
    		if(ItemMatches(item, stack))
    			count += stack.getMaxStackSize() - stack.getCount();
    		else if(stack.isEmpty())
    			count += stack.getMaxStackSize();
    	}
    	return count;
    }
    
    public static int GetItemTagCount(Container inventory, TagKey<Item> itemTag, Item... blacklistItems)
    {
    	List<Item> blacklist = Lists.newArrayList(blacklistItems);
    	int count = 0;
    	for(int i = 0; i < inventory.getContainerSize(); i++)
    	{
    		ItemStack stack = inventory.getItem(i);
    		if(ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()))
    			count += stack.getCount();
    	}
    	return count;
    }
    
    public static boolean RemoveItemTagCount(Container inventory, TagKey<Item> itemTag, int count, Item... blacklistItems)
    {
    	if(GetItemTagCount(inventory, itemTag, blacklistItems) < count)
    		return false;
    	List<Item> blacklist = Lists.newArrayList(blacklistItems);
    	for(int i = 0; i < inventory.getContainerSize(); i++)
    	{
    		ItemStack stack = inventory.getItem(i);
    		if(ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()))
    		{
    			int amountToTake = MathUtil.clamp(count, 0, stack.getCount());
    			count-= amountToTake;
    			if(amountToTake == stack.getCount())
    				inventory.setItem(i, ItemStack.EMPTY);
    			else
    				stack.shrink(amountToTake);
    		}
    	}
    	return true;
    }
    
    /**
     * Places a given item stack in the inventory. Will not place if there's no room for every item.
     * @return Whether the stack was placed in the inventory. If false was returned nothing was placed.
     */
    public static boolean PutItemStack(Container inventory, ItemStack stack)
    {
    	int amountToMerge = stack.getCount();
		Item mergeItem = stack.getItem();
		List<Pair<Integer,Integer>> mergeOrders = new ArrayList<>();
		//First pass, looking for stacks to add to
    	for(int i = 0; i < inventory.getContainerSize() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getItem(i);
			if(ItemMatches(stack, inventoryStack) && inventoryStack.getCount() != inventoryStack.getMaxStackSize())
			{
				//Calculate the amount that can fit in this slot
				int amountToPlace = MathUtil.clamp(amountToMerge, 0, inventoryStack.getMaxStackSize() - inventoryStack.getCount());
				//Define the orders
				mergeOrders.add(new Pair<Integer,Integer>(i,amountToPlace));
				//Update the pending merge count
				amountToMerge -= amountToPlace;
			}
		}
    	//Second pass, checking for empty slots to place them in
		for(int i = 0; i < inventory.getContainerSize() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getItem(i);
			if(inventoryStack.isEmpty())
			{
				//Calculate the amount that can fit in this slot
				int amountToPlace = MathUtil.clamp(amountToMerge, 0, stack.getMaxStackSize());
				//Define the orders
				mergeOrders.add(new Pair<Integer,Integer>(i,amountToPlace));
				//Update the pending merge count
				amountToMerge -= amountToPlace;
			}
		}
		//Confirm that all items have a placed to be placed
    	if(amountToMerge > 0)
    		return false;
    	//Execute item placement/addition
    	mergeOrders.forEach(order ->
		{
			ItemStack itemStack = inventory.getItem(order.getFirst());
			if(itemStack.isEmpty())
			{
				ItemStack newStack = new ItemStack(mergeItem, order.getSecond());
				if(stack.hasTag())
					newStack.setTag(stack.getTag().copy());
				inventory.setItem(order.getFirst(), newStack);
			}
			else
			{
				itemStack.setCount(itemStack.getCount() + order.getSecond());
			}
		});
    	
    	return true;
    }
    
    /**
     * Places as much of the given item stack as possible into the inventory.
     * @return The remaining items that were unable to be placed.
     */
    public static ItemStack TryPutItemStack(Container inventory, ItemStack stack)
    {
    	int amountToMerge = stack.getCount();
		//First pass, looking for stacks to add to
    	for(int i = 0; i < inventory.getContainerSize() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getItem(i);
			if(ItemMatches(stack, inventoryStack) && inventoryStack.getCount() < inventoryStack.getMaxStackSize())
			{
				//Calculate the amount that can fit in this slot
				int amountToPlace = MathUtil.clamp(amountToMerge, 0, inventoryStack.getMaxStackSize() - inventoryStack.getCount());
				//Add the items to the stack
				inventoryStack.grow(amountToPlace);
				//Set the stack in the slot to trigger the markDirty code
				inventory.setItem(i,inventoryStack);
				//Update the pending merge count
				amountToMerge -= amountToPlace;
			}
		}
    	//Second pass, checking for empty slots to place them in
		for(int i = 0; i < inventory.getContainerSize() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getItem(i);
			if(inventoryStack.isEmpty())
			{
				//Calculate the amount that can fit in this slot
				int amountToPlace = MathUtil.clamp(amountToMerge, 0, stack.getMaxStackSize());
				//Place a new stack in the empty slot
				ItemStack newStack = stack.copy();
				newStack.setCount(amountToPlace);
				inventory.setItem(i, newStack);
				//Update the pending merge count
				amountToMerge -= amountToPlace;
			}
		}
    	
    	if(amountToMerge > 0)
    	{
    		ItemStack leftovers = stack.copy();
    		leftovers.setCount(amountToMerge);
    		return leftovers;
    	}
    	return ItemStack.EMPTY;
    }
    
    /**
     * Determines whether there is enough room in the inventory to place the requested item stacks
     */
    public static boolean CanPutItemStack(Container inventory, ItemStack stack)
    {
    	if(stack.isEmpty())
    		return true;
    	int amountToMerge = stack.getCount();
		//First pass, looking for stacks to add to
    	for(int i = 0; i < inventory.getContainerSize() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getItem(i);
			if(ItemMatches(stack, inventoryStack) && inventoryStack.getCount() != inventoryStack.getMaxStackSize())
			{
				//Calculate the amount that can fit in this slot
				int amountToPlace = MathUtil.clamp(amountToMerge, 0, inventoryStack.getMaxStackSize() - inventoryStack.getCount());
				//Update the pending merge count
				amountToMerge -= amountToPlace;
			}
		}
    	//Second pass, checking for empty slots to place them in
		for(int i = 0; i < inventory.getContainerSize() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getItem(i);
			if(inventoryStack.isEmpty())
			{
				//Calculate the amount that can fit in this slot
				int amountToPlace = MathUtil.clamp(amountToMerge, 0, stack.getMaxStackSize());
				//Update the pending merge count
				amountToMerge -= amountToPlace;
			}
		}
		return amountToMerge <= 0;
    }
    
    public static boolean CanPutItemStacks(Container inventory, ItemStack... stacks) { return CanPutItemStacks(inventory, Lists.newArrayList(stacks)); }
    
    public static boolean CanPutItemStacks(Container inventory, List<ItemStack> stacks)
    {
    	Container copyInventory = new SimpleContainer(inventory.getContainerSize());
    	for(int i = 0; i < inventory.getContainerSize(); ++i)
    		copyInventory.setItem(i, inventory.getItem(i).copy());
    	for(int i = 0; i < stacks.size(); ++i)
    	{
    		if(!InventoryUtil.PutItemStack(copyInventory, stacks.get(i)))
    			return false;
    	}
    	return true;
    }
    
    /**
     * Merges item stacks of the same type together (e.g. 2 stacks of 32 cobblestone will become 1 stack of 64 cobblestone and an extra empty slot)
     */
    public static void MergeStacks(Container inventory)
	{
		for(int i = 0; i < inventory.getContainerSize(); i++)
		{
			ItemStack thisStack = inventory.getItem(i);
			if(!thisStack.isEmpty())
			{
				int amountWanted = thisStack.getMaxStackSize() - thisStack.getCount();
				if(amountWanted > 0)
				{
					//Steal from further stacks
					for(int j = i + 1; j < inventory.getContainerSize(); j++)
					{
						ItemStack nextStack = inventory.getItem(j);
						if(!nextStack.isEmpty() && nextStack.getItem() == thisStack.getItem() && ItemStackHelper.TagEquals(thisStack, nextStack))
						{
							while(amountWanted > 0 && !nextStack.isEmpty())
							{
								nextStack.setCount(nextStack.getCount() - 1);
								thisStack.setCount(thisStack.getCount() + 1);
								amountWanted--;
							}
						}
					}
				}
			}
		}
	}

	public static void saveAllItems(String key, CompoundTag compound, Container inventory)
	{
		ItemStackHelper.saveAllItems(key, compound, buildList(inventory));
	}

    public static SimpleContainer loadAllItems(String key, CompoundTag compound, int inventorySize)
    {
    	NonNullList<ItemStack> tempInventory = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
    	ItemStackHelper.loadAllItems(key, compound, tempInventory);
    	return buildInventory(tempInventory);
    }

	public static void saveItemList(@Nonnull String key, @Nonnull CompoundTag compound, @Nonnull List<ItemStack> itemList)
	{
		ListTag list = new ListTag();
		for(ItemStack item : itemList)
		{
			if (!item.isEmpty()) {
				CompoundTag itemTag = new CompoundTag();
				item.save(itemTag);
				itemTag.putInt("Count", item.getCount());
				list.add(itemTag);
			}
		}
		compound.put(key,list);
	}

	@Nonnull
	public static List<ItemStack> loadItemList(@Nonnull String key, @Nonnull CompoundTag compound)
	{
		List<ItemStack> result = new ArrayList<>();
		if(compound.contains(key, Tag.TAG_LIST))
		{
			ListTag list = compound.getList(key, Tag.TAG_COMPOUND);
			for(int i = 0; i < list.size(); ++i)
			{
				CompoundTag itemTag = list.getCompound(i);
				ItemStack item = ItemStack.of(itemTag);
				item.setCount(itemTag.getInt("Count"));
				if(!item.isEmpty())
					result.add(item);
			}
		}
		return result;
	}

	public static void encodeItems(Container inventory, FriendlyByteBuf buffer) {
		CompoundTag tag = new CompoundTag();
		saveAllItems("ITEMS", tag, inventory);
		buffer.writeInt(inventory.getContainerSize());
		buffer.writeNbt(tag);
	}

	public static SimpleContainer decodeItems(FriendlyByteBuf buffer) {
		int inventorySize = buffer.readInt();
		return loadAllItems("ITEMS", buffer.readAnySizeNbt(), inventorySize);
	}

	public static SimpleContainer copy(Container inventory) {
		SimpleContainer copy = new SimpleContainer(inventory.getContainerSize());
		for(int i = 0; i < inventory.getContainerSize(); ++i)
			copy.setItem(i, inventory.getItem(i).copy());
		return copy;
	}

    public static void dumpContents(Level level, BlockPos pos, Container inventory)
    {
    	if(level.isClientSide)
			return;
    	for(int i = 0; i < inventory.getContainerSize(); i++)
    		dumpContents(level, pos, inventory.getItem(i));
    }
    
    public static void dumpContents(Level level, BlockPos pos, List<ItemStack> inventory)
    {
    	if(level.isClientSide)
    		return;
		for (ItemStack itemStack : inventory)
			dumpContents(level, pos, itemStack);
    }
    
    public static void dumpContents(Level level, BlockPos pos, ItemStack stack)
    {
    	if(level.isClientSide)
    		return;
    	if(!stack.isEmpty())
		{
			ItemEntity entity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);
			level.addFreshEntity(entity);
		}
    }
    
    public static List<ItemStack> combineQueryItems(ItemStack... items) { return combineQueryItems(Lists.newArrayList(items)); }

	public static List<ItemStack> combineQueryItems(List<ItemStack> items)
	{
		List<ItemStack> itemList = new ArrayList<>();
		for(ItemStack item : items)
		{
			boolean addNew = true;
			for(int i = 0; i < itemList.size() && addNew; ++i)
			{
				if(ItemMatches(item, itemList.get(i)))
				{
					itemList.get(i).grow(item.getCount());
					addNew = false;
				}
			}
			if(addNew && !item.isEmpty())
				itemList.add(item.copy());
		}
		return itemList;
	}

	public static List<ItemRequirement> combineRequirements(ItemRequirement... requirements)
	{
		List<ItemRequirement> list = new ArrayList<>();
		for(ItemRequirement requirement : requirements)
		{
			if(!requirement.isNull())
			{
				for (ItemRequirement r : list) {
					if(r.tryMerge(requirement))
						break;
				}
				if(!requirement.isNull())
					list.add(requirement);
			}
		}
		return list;
	}
    
    /**
     * Determines whether the two item stacks are the same item/nbt. Ignores quantity of the items in the stack
     */
    public static boolean ItemMatches(ItemStack stack1, ItemStack stack2)
    {
    	if(stack1.getItem() == stack2.getItem())
    		return ItemStackHelper.TagEquals(stack1, stack2);
    	return false;
    }

	public static boolean ItemsFullyMatch(ItemStack stack1, ItemStack stack2) { return ItemMatches(stack1, stack2) && stack1.getCount() == stack2.getCount(); }

	public static boolean ContainerMatches(@Nonnull List<ItemStack> list1, @Nonnull List<ItemStack> list2)
	{
		if(list1.size() != list2.size())
			return false;
		for(int i = 0; i < list1.size(); ++i)
		{
			if(!ItemsFullyMatch(list1.get(i),list2.get(i)))
				return false;
		}
		return true;
	}

	public static boolean ContainerMatches(@Nonnull Container container1, @Nonnull Container container2)
	{
		if(container1.getContainerSize() != container2.getContainerSize())
			return false;
		for(int i = 0; i < container1.getContainerSize(); ++i)
		{
			if(!ItemsFullyMatch(container1.getItem(i), container2.getItem(i)))
				return false;
		}
		return true;
	}
    
    public static boolean ItemHasTag(ItemStack item, TagKey<Item> tag) { return item.getTags().anyMatch(t -> t.equals(tag)); }

	@Nonnull
	public static List<Item> GetItemsWithTag(@Nonnull TagKey<Item> tag) { return ForgeRegistries.ITEMS.tags().getTag(tag).stream().toList(); }

	@Nonnull
	public static List<ItemStack> GetItemStacksWithTag(@Nonnull TagKey<Item> tag) { return GetItemsWithTag(tag).stream().map(ItemStack::new).toList(); }

    public static int safeGiveToPlayer(Inventory inv, ItemStack stack) {
    	
    	int i = inv.getSlotWithRemainingSpace(stack);
      	if (i == -1)
      		i = inv.getFreeSlot();

     	if(i >= 0)
     	{
     		ItemStack stackInSlot = inv.getItem(i);
     		int putCount = Math.min(stack.getCount(), stackInSlot.isEmpty() ? stack.getMaxStackSize() : stackInSlot.getMaxStackSize() - stackInSlot.getCount());
     		if(putCount > 0)
     		{
     			if(stackInSlot.isEmpty())
     			{
     				stackInSlot = stack.copy();
     				stackInSlot.setCount(putCount);
     			}
     			else
     				stackInSlot.grow(putCount);
     			stack.shrink(putCount);
     			inv.setItem(i, stackInSlot);
     			inv.setChanged();
     		}
     		return putCount;
     	}
     	else
     		return 0;
    }

	public static int totalItemCount(@Nonnull List<ItemStack> list)
	{
		int count = 0;
		for(ItemStack s : list)
			count += s.getCount();
		return count;
	}
    
}
