package io.github.lightman314.lightmanscurrency.util;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.*;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class InventoryUtil {

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
			copy.setItem(i,inventory.getItem(i).copy());
		return copy;
	}

    public static ItemStack insertItemStacked(Container container,ItemStack stack,boolean simulate) {
        return ItemHandlerHelper.insertItemStacked(new InvWrapper(container),stack,simulate);
    }

    public static ItemStack insertItem(Container container,ItemStack stack,boolean simulate) {
        return ItemHandlerHelper.insertItem(new InvWrapper(container),stack,simulate);
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
