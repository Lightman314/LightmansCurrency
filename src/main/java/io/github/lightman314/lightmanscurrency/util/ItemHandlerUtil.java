package io.github.lightman314.lightmanscurrency.util;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ItemHandlerUtil {

    public static List<ItemStack> copyList(List<ItemStack> list) { return ListUtil.copyList(list,ItemStack::copy); }

    public static LCItemStackHandler fromList(List<ItemStack> list) { return new LCItemStackHandler(list); }

    public static List<ItemStack> toList(IItemHandler inventory) {
        List<ItemStack> list = new ArrayList<>(inventory.getSlots());
        for(int i = 0; i < inventory.getSlots();++i)
            list.add(inventory.getStackInSlot(i).copy());
        return list;
    }

    public static List<ItemStack> extractItem(IItemHandler handler, Predicate<ItemStack> test, int count, boolean simulate)
    {
        List<ItemStack> result = new ArrayList<>();
        for(int i = 0; i < handler.getSlots() && count > 0; ++i)
        {
            ItemStack s = handler.extractItem(i,count,true);
            if(test.test(s))
            {
                if(!simulate)
                    s = handler.extractItem(i,count,false);
                count -= s.getCount();
                result.add(s);
            }
        }
        return combineStacks(result);
    }

    public static ItemStack extractItem(List<IItemHandler> handlers,ItemStack stack,boolean simulate)
    {
        ItemStack result = ItemStack.EMPTY;
        ItemStack extractStack = stack.copy();
        for(IItemHandler handler : handlers)
        {
            //End early if we have extracted everything we need
            if(extractStack.isEmpty())
                return result;
            ItemStack e = extractItem(handler,extractStack,simulate);
            if(!e.isEmpty())
            {
                if(result.isEmpty())
                    result = e;
                else
                    result.grow(e.getCount());
                extractStack.shrink(e.getCount());
            }
        }
        return result;
    }

    public static ItemStack extractItem(IItemHandler handler,ItemStack stack,boolean simulate)
    {
        int extractAmount = stack.getCount();
        ItemStack result = ItemStack.EMPTY;
        for(int i = 0; i < handler.getSlots() && extractAmount > 0; ++i)
        {
            ItemStack stackInSlot = handler.getStackInSlot(i);
            if(ItemStack.isSameItemSameComponents(stack,stackInSlot))
            {
                ItemStack extracted = handler.extractItem(i,extractAmount,true);
                if(!extracted.isEmpty() && ItemStack.isSameItemSameComponents(stack,extracted))
                {
                    if(!simulate)
                        extracted = handler.extractItem(i,extractAmount,false);
                    int amount = extracted.getCount();
                    extractAmount -= amount;
                    if(result.isEmpty())
                        result = stack.copyWithCount(amount);
                    else
                        result.grow(amount);
                }
            }
        }
        return result;
    }

    public static ItemStack insertItem(List<IItemHandler> handlers,ItemStack stack,boolean simulate)
    {
        int insertAmount = stack.getCount();
        for(IItemHandler handler : handlers)
        {
            stack = ItemHandlerHelper.insertItemStacked(handler,stack,simulate);
            if(stack.isEmpty())
                return stack;
        }
        return stack;
    }

    public static int getItemCount(IItemHandler handler,Predicate<ItemStack> test)
    {
        int count = 0;
        for(int i = 0; i < handler.getSlots(); ++i)
        {
            ItemStack s = handler.extractItem(i,Integer.MAX_VALUE,true);
            if(test.test(s))
                count += s.getCount();
        }
        return count;
    }

    public static int getItemCount(IItemHandler handler,ItemStack stack)
    {
        int count = 0;
        for(int i = 0; i < handler.getSlots(); ++i)
        {
            ItemStack s = handler.extractItem(i,Integer.MAX_VALUE,true);
            if(ItemStack.isSameItemSameComponents(stack,s))
                count += s.getCount();
        }
        return count;
    }

    public static int getItemCount(List<ItemStack> list,ItemStack stack)
    {
        int count = 0;
        for(ItemStack s : list)
        {
            if(ItemStack.isSameItemSameComponents(s,stack))
                count += s.getCount();
        }
        return count;
    }

    public static int getItemCount(List<ItemStack> list) {
        int count = 0;
        for(ItemStack s : list)
            count += s.getCount();
        return count;
    }

    public static List<ItemStack> combineStacks(ItemStack... items) { return combineStacks(Lists.newArrayList(items)); }
    public static List<ItemStack> combineStacks(List<ItemStack> list)
    {
        List<ItemStack> results = new ArrayList<>();
        list = new ArrayList<>(list);
        while(!list.isEmpty())
        {
            ItemStack stack = list.getFirst().copy();
            list.removeFirst();
            if(stack.isEmpty())
                continue;
            for(int i = 0; i < list.size(); ++i)
            {
                ItemStack s2 = list.get(i);
                if(ItemStack.isSameItemSameComponents(stack,s2))
                {
                    stack.setCount(stack.getCount() + s2.getCount());
                    list.remove(i--);
                }
            }
            results.add(stack);
        }
        return results;
    }

    public static boolean isEmpty(IItemHandler handler)
    {
        for(int i = 0; i < handler.getSlots(); ++i)
        {
            if(!handler.getStackInSlot(i).isEmpty())
                return false;
        }
        return true;
    }

    public static boolean equals(IItemHandler handler1,IItemHandler handler2)
    {
        if(handler1.getSlots() == handler2.getSlots())
        {
            for(int i = 0; i < handler1.getSlots();++i)
            {
                ItemStack s1 = handler1.getStackInSlot(i);
                ItemStack s2 = handler2.getStackInSlot(i);
                if(!ItemStack.isSameItemSameComponents(s1,s2) || s1.getCount() != s2.getCount())
                    return false;
            }
            //All Items match
            return true;
        }
        return false;
    }

    public static void dumpContents(Level level,BlockPos pos,IItemHandler inventory) {
        for(int i = 0; i < inventory.getSlots(); ++i)
        {
            ItemStack removed = inventory.extractItem(i,Integer.MAX_VALUE,false);
            if(!removed.isEmpty())
                dumpContents(level,pos,removed);
        }
    }

    public static void dumpContents(Level level,BlockPos pos,List<ItemStack> inventory)
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

    public static boolean isExactMatch(ItemStack stack1, ItemStack stack2) { return ItemStack.isSameItemSameComponents(stack1,stack2) && stack1.getCount() == stack2.getCount(); }

}
