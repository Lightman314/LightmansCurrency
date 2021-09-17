package io.github.lightman314.lightmanscurrency.util;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InventoryUtil {

	
	public static IInventory buildInventory(List<ItemStack> list)
	{
		IInventory inventory = new Inventory(list.size());
		for(int i = 0; i < list.size(); i++)
		{
			inventory.setInventorySlotContents(i, list.get(i).copy());
		}
		return inventory;
	}
	
	public static IInventory buildInventory(ItemStack stack)
	{
		IInventory inventory = new Inventory(1);
		inventory.setInventorySlotContents(0, stack);
		return inventory;
	}
	
	public static IInventory copyInventory(IInventory inventory)
	{
		IInventory copy = new Inventory(inventory.getSizeInventory());
		for(int i = 0; i < inventory.getSizeInventory(); i++)
		{
			copy.setInventorySlotContents(i, inventory.getStackInSlot(i).copy());
		}
		return copy;
	}
	
	public static NonNullList<ItemStack> buildList(IInventory inventory)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
		for(int i = 0; i < inventory.getSizeInventory(); i++)
		{
			list.set(i, inventory.getStackInSlot(i).copy());
		}
		return list;
	}
	
	/**
     * Gets the quantity of a specific item in the given inventory
     * Ignores NBT data, as none is given
     */
    public static int GetItemCount(IInventory inventory, Item item)
    {
    	int count = 0;
    	for(int i = 0; i < inventory.getSizeInventory(); i++)
    	{
    		ItemStack stack = inventory.getStackInSlot(i);
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
    public static int GetItemCount(IInventory inventory, ItemStack item)
    {
    	int count = 0;
    	for(int i = 0; i < inventory.getSizeInventory(); i++)
    	{
    		ItemStack stack = inventory.getStackInSlot(i);
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
    public static boolean RemoveItemCount(IInventory inventory, Item item, int count)
    {
    	for(int i = 0; i < inventory.getSizeInventory(); i++)
    	{
    		ItemStack stack = inventory.getStackInSlot(i);
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
    				inventory.setInventorySlotContents(i, ItemStack.EMPTY);
    			}
    		}
    	}
    	return count <= 0;
    }
	
    /**
     * Removes the given item stack from the given inventory, validating nbt data.
     * @return Whether the full amount of items were successfully taken.
     */
    public static boolean RemoveItemCount(IInventory inventory, ItemStack item)
    {
    	if(GetItemCount(inventory, item) < item.getCount())
    		return false;
    	int count = item.getCount();
    	for(int i = 0; i < inventory.getSizeInventory(); i++)
    	{
    		ItemStack stack = inventory.getStackInSlot(i);
    		if(ItemMatches(stack, item))
    		{
    			int amountToTake = MathUtil.clamp(count, 0, stack.getCount());
    			count -= amountToTake;
    			if(amountToTake == stack.getCount())
    				inventory.setInventorySlotContents(i, ItemStack.EMPTY);
    			else
    				stack.shrink(amountToTake);
    		}
    	}
    	return true;
    }
    
    public static int GetItemTagCount(IInventory inventory, ResourceLocation itemTag)
    {
    	int count = 0;
    	for(int i = 0; i < inventory.getSizeInventory(); i++)
    	{
    		ItemStack stack = inventory.getStackInSlot(i);
    		if(stack.getItem().getTags().contains(itemTag))
    			count += stack.getCount();
    	}
    	return count;
    }
    
    public static boolean RemoveItemTagCount(IInventory inventory, ResourceLocation itemTag, int count)
    {
    	if(GetItemTagCount(inventory, itemTag) < count)
    		return false;
    	for(int i = 0; i < inventory.getSizeInventory(); i++)
    	{
    		ItemStack stack = inventory.getStackInSlot(i);
    		if(stack.getItem().getTags().contains(itemTag))
    		{
    			int amountToTake = MathUtil.clamp(count, 0, stack.getCount());
    			count-= amountToTake;
    			if(amountToTake == stack.getCount())
    				inventory.setInventorySlotContents(i, ItemStack.EMPTY);
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
    public static boolean PutItemStack(IInventory inventory, ItemStack stack)
    {
    	int amountToMerge = stack.getCount();
		Item mergeItem = stack.getItem();
		List<Pair<Integer,Integer>> mergeOrders = new ArrayList<>();
		//First pass, looking for stacks to add to
    	for(int i = 0; i < inventory.getSizeInventory() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getStackInSlot(i);
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
		for(int i = 0; i < inventory.getSizeInventory() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getStackInSlot(i);
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
			ItemStack itemStack = inventory.getStackInSlot(order.getFirst());
			if(itemStack.isEmpty())
			{
				ItemStack newStack = new ItemStack(mergeItem, order.getSecond());
				if(stack.hasTag())
					newStack.setTag(stack.getTag().copy());
				inventory.setInventorySlotContents(order.getFirst(), newStack);
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
    public static ItemStack TryPutItemStack(IInventory inventory, ItemStack stack)
    {
    	int amountToMerge = stack.getCount();
		Item mergeItem = stack.getItem();
		//First pass, looking for stacks to add to
    	for(int i = 0; i < inventory.getSizeInventory() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getStackInSlot(i);
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
		for(int i = 0; i < inventory.getSizeInventory() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getStackInSlot(i);
			if(inventoryStack.isEmpty())
			{
				//Calculate the amount that can fit in this slot
				int amountToPlace = MathUtil.clamp(amountToMerge, 0, stack.getMaxStackSize());
				//Place a new stack in the empty slot
				ItemStack newStack = new ItemStack(mergeItem, amountToPlace);
				if(stack.hasTag())
					newStack.setTag(stack.getTag().copy());
				inventory.setInventorySlotContents(i, newStack);
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
    public static boolean CanPutItemStack(IInventory inventory, ItemStack stack)
    {
    	int amountToMerge = stack.getCount();
		//First pass, looking for stacks to add to
    	for(int i = 0; i < inventory.getSizeInventory() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getStackInSlot(i);
			if(ItemMatches(stack, inventoryStack) && inventoryStack.getCount() != inventoryStack.getMaxStackSize())
			{
				//Calculate the amount that can fit in this slot
				int amountToPlace = MathUtil.clamp(amountToMerge, 0, inventoryStack.getMaxStackSize() - inventoryStack.getCount());
				//Update the pending merge count
				amountToMerge -= amountToPlace;
			}
		}
    	//Second pass, checking for empty slots to place them in
		for(int i = 0; i < inventory.getSizeInventory() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = inventory.getStackInSlot(i);
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
    public static void MergeStacks(IInventory inventory)
	{
		for(int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack thisStack = inventory.getStackInSlot(i);
			if(!thisStack.isEmpty())
			{
				int amountWanted = thisStack.getMaxStackSize() - thisStack.getCount();
				if(amountWanted > 0)
				{
					//Steal from further stacks
					for(int j = i + 1; j < inventory.getSizeInventory(); j++)
					{
						ItemStack nextStack = inventory.getStackInSlot(j);
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
    
    public static IInventory loadAllItems(String key, CompoundNBT compound, int inventorySize)
    {
    	NonNullList<ItemStack> tempInventory = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
    	ItemStackHelper.loadAllItems(key, compound, tempInventory);
    	return buildInventory(tempInventory);
    }
    
    public static void saveAllItems(String key, CompoundNBT compound, IInventory inventory)
    {
    	ItemStackHelper.saveAllItems(key, compound, buildList(inventory));
    }
    
    public static void dumpContents(World world, BlockPos pos, IInventory inventory)
    {
    	if(world.isRemote)
			return;
    	InventoryHelper.dropInventoryItems(world, pos, inventory);
    }
    
    public static void dumpContents(World world, BlockPos pos, List<ItemStack> inventory)
    {
    	if(world.isRemote)
			return;
    	InventoryHelper.dropInventoryItems(world, pos, InventoryUtil.buildInventory(inventory));
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
