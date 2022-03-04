package io.github.lightman314.lightmanscurrency.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InventoryUtil {

	
	public static Container buildInventory(List<ItemStack> list)
	{
		Container inventory = new SimpleContainer(list.size());
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
		{
			copy.setItem(i, inventory.getItem(i).copy());
		}
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
    		{
    			count += stack.getCount();
    		}
    	}
    	return count;
    }
    
    /**
     * Gets the quantity of a specific item in the given container section validating NBT data where applicable
     */
    public static int GetItemCount(Container inventory, ItemStack item, int startIndex, int stopIndex)
    {
    	int count = 0;
    	for(int i = startIndex; i < stopIndex && i < inventory.getContainerSize(); i++)
    	{
    		ItemStack stack = inventory.getItem(i);
    		if(ItemMatches(stack, item))
    		{
    			count += stack.getCount();
    		}
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
    public static boolean RemoveItemCount(Container inventory, ItemStack item, int startIndex, int stopIndex)
    {
    	if(GetItemCount(inventory, item, startIndex, stopIndex) < item.getCount())
    		return false;
    	int count = item.getCount();
    	for(int i = startIndex; i < stopIndex && i < inventory.getContainerSize(); i++)
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
    
    /**
     * Puts the item stack into the given container.
     * @return The any portion of the item that wasn't able to be placed in the container.
     */
    public static ItemStack PutItemInSlot(Container container, ItemStack item)
    {
    	return PutItemInSlot(container, item, 0, container.getContainerSize());
    }
    
    /**
     * Puts the item stack into the given portion of the container.
     * @return The any portion of the item that wasn't able to be placed in the container.
     */
    public static ItemStack PutItemInSlot(Container container, ItemStack item, int startingIndex, int stopIndex)
    {
    	ItemStack copyStack = item.copy();
    	for(int i = startingIndex; i < stopIndex && i < container.getContainerSize() && !copyStack.isEmpty(); ++i)
    	{
    		ItemStack stack = container.getItem(i);
    		if(ItemMatches(copyStack, stack))
    		{
    			int addAmount = Math.min(stack.getMaxStackSize() - stack.getCount(), copyStack.getCount());
    			stack.grow(addAmount);
    			copyStack.shrink(addAmount);
    		}
    		else if(stack.isEmpty())
    		{
    			container.setItem(i, copyStack);
    			copyStack = ItemStack.EMPTY;
    		}
    	}
    	return copyStack;
    }
    
    public static int GetItemTagCount(Container inventory, ResourceLocation itemTag, Item... blacklistItems)
    {
    	List<Item> blacklist = Lists.newArrayList(blacklistItems);
    	int count = 0;
    	for(int i = 0; i < inventory.getContainerSize(); i++)
    	{
    		ItemStack stack = inventory.getItem(i);
    		if(stack.getItem().getTags().contains(itemTag) && !blacklist.contains(stack.getItem()))
    			count += stack.getCount();
    	}
    	return count;
    }
    
    public static boolean RemoveItemTagCount(Container inventory, ResourceLocation itemTag, int count, Item... blacklistItems)
    {
    	if(GetItemTagCount(inventory, itemTag, blacklistItems) < count)
    		return false;
    	List<Item> blacklist = Lists.newArrayList(blacklistItems);
    	for(int i = 0; i < inventory.getContainerSize(); i++)
    	{
    		ItemStack stack = inventory.getItem(i);
    		if(stack.getItem().getTags().contains(itemTag) && !blacklist.contains(stack.getItem()))
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
		Item mergeItem = stack.getItem();
		//First pass, looking for stacks to add to
    	for(int i = 0; i < inventory.getContainerSize() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getItem(i);
			if(ItemMatches(stack, inventoryStack) && inventoryStack.getCount() != inventoryStack.getMaxStackSize())
			{
				//Calculate the amount that can fit in this slot
				int amountToPlace = MathUtil.clamp(amountToMerge, 0, inventoryStack.getMaxStackSize() - inventoryStack.getCount());
				//Add the items to the stack
				inventoryStack.grow(amountToPlace);
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
				ItemStack newStack = new ItemStack(mergeItem, amountToPlace);
				if(stack.hasTag())
					newStack.setTag(stack.getTag().copy());
				inventory.setItem(i, newStack);
				//Update the pending merge count
				amountToMerge -= amountToPlace;
			}
		}
    	
    	if(amountToMerge > 0)
    		return new ItemStack(mergeItem, amountToMerge);
    	return ItemStack.EMPTY;
    }
    
    /**
     * Determines whether there is enough room in the inventory to place the requested item stacks
     * @param inventory
     * @param stack
     * @return
     */
    public static boolean CanPutItemStack(Container inventory, ItemStack stack)
    {
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
    
    /**
     * Merges item stacks of the same type together (e.g. 2 stacks of 32 cobblestone will become 1 stack of 64 cobblestone and an extra empty slot)
     * @param inventory
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
    
    public static Container loadAllItems(String key, CompoundTag compound, int inventorySize)
    {
    	NonNullList<ItemStack> tempInventory = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
    	ItemStackHelper.loadAllItems(key, compound, tempInventory);
    	return buildInventory(tempInventory);
    }
    
    public static void saveAllItems(String key, CompoundTag compound, Container inventory)
    {
    	ItemStackHelper.saveAllItems(key, compound, buildList(inventory));
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
    	for(int i = 0; i < inventory.size(); i++)
    		dumpContents(level, pos, inventory.get(i));
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
    
    /**
     * Determines whether the two item stacks are the same item/nbt. Ignores quantity of the items in the stack
     */
    public static boolean ItemMatches(ItemStack stack1, ItemStack stack2)
    {
    	if(stack1.getItem() == stack2.getItem())
    		return ItemStackHelper.TagEquals(stack1, stack2);
    	return false;
    }
    
}
