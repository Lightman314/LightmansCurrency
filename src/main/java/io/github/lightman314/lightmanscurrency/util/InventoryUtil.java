package io.github.lightman314.lightmanscurrency.util;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.*;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InventoryUtil {

    @Deprecated
	public static SimpleContainer buildInventory(List<ItemStack> list)
	{
		SimpleContainer inventory = new SimpleContainer(list.size());
		for(int i = 0; i < list.size(); i++)
		{
			inventory.setItem(i, list.get(i).copy());
		}
		return inventory;
	}

    @Deprecated
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

	public static SimpleContainer copy(Container inventory) {
		SimpleContainer copy = new SimpleContainer(inventory.getContainerSize());
		for(int i = 0; i < inventory.getContainerSize(); ++i)
			copy.setItem(i, inventory.getItem(i).copy());
		return copy;
	}

    public static ItemStack insertItemStacked(Container container,ItemStack stack,boolean simulate)
    {
        Container active = simulate ? copy(container) : container;
        stack = stack.copy();
        for(int i = 0; i < active.getContainerSize() && !stack.isEmpty(); ++i)
        {
            //Look for items with matching stacks
            ItemStack sis = active.getItem(i);
            if(ItemStack.isSameItemSameComponents(sis,stack))
            {
                int maxCount = container.getMaxStackSize(stack);
                int space = sis.isEmpty() ? maxCount : maxCount - sis.getCount();
                int insertAmount = Math.min(space,stack.getCount());
                if(insertAmount > 0)
                {
                    ItemStack newStack = stack.copyWithCount(sis.getCount() + insertAmount);
                    if(container.canPlaceItem(i,newStack))
                    {
                        active.setItem(i,newStack);
                        stack.shrink(insertAmount);
                    }
                }
            }
        }
        //Otherwise insert normally
        for(int i = 0; i < active.getContainerSize() && !stack.isEmpty(); ++i)
        {
            ItemStack sis = active.getItem(i);
            if(sis.isEmpty())
            {
                int space = container.getMaxStackSize(stack);
                int insertAmount = Math.min(space,stack.getCount());
                if(insertAmount > 0)
                {
                    ItemStack newStack = stack.copyWithCount(sis.getCount() + insertAmount);
                    if(container.canPlaceItem(i,newStack))
                    {
                        active.setItem(i,newStack);
                        stack.shrink(insertAmount);
                    }
                }
            }
        }
        return stack;
    }

    public static ItemStack insertItem(Container container,ItemStack stack,boolean simulate)
    {
        if(stack.isEmpty())
            return ItemStack.EMPTY;
        Container active = simulate ? copy(container) : container;
        stack = stack.copy();
        for(int i = 0; i < active.getContainerSize() && !stack.isEmpty(); ++i)
        {
            ItemStack sis = active.getItem(i);
            if(sis.isEmpty() || ItemStack.isSameItemSameComponents(sis,stack))
            {
                int maxCount = container.getMaxStackSize(stack);
                int space = sis.isEmpty() ? maxCount : maxCount - sis.getCount();
                int insertAmount = Math.min(space,stack.getCount());
                if(insertAmount > 0)
                {
                    ItemStack newStack = stack.copyWithCount(sis.getCount() + insertAmount);
                    if(container.canPlaceItem(i,newStack))
                    {
                        active.setItem(i,newStack);
                        stack.shrink(insertAmount);
                    }
                }
            }
        }
        return stack;
    }

    public static void dropContents(Level level, BlockPos pos, Container inventory)
    {
        if(level.isClientSide)
            return;
        for(int i = 0; i < inventory.getContainerSize(); i++)
            dropContents(level, pos, inventory.getItem(i));
    }

    public static void dropContents(Level level, BlockPos pos, List<ItemStack> inventory)
    {
        if(level.isClientSide)
            return;
        for (ItemStack itemStack : inventory)
            dropContents(level, pos, itemStack);
    }

    public static void dropContents(Level level, BlockPos pos, ItemStack stack)
    {
        if(level.isClientSide)
            return;
        if(!stack.isEmpty())
        {
            ItemEntity entity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            level.addFreshEntity(entity);
        }
    }
    
}
