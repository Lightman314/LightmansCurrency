package io.github.lightman314.lightmanscurrency.util;

import com.google.common.collect.Lists;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;

import java.util.*;
import java.util.function.Predicate;

public final class ItemRequirement implements Predicate<ItemStack> {

    public static final ItemRequirement NULL = of((s) -> false, 0);

    public final Predicate<ItemStack> filter;
    public final int count;
    private ItemRequirement(Predicate<ItemStack> filter, int count) { this.filter = filter; this.count = count; }
    @Override
    public boolean test(ItemStack stack) { return this.filter.test(stack); }
    public boolean isNull() { return this == NULL; }

    public ItemRequirement merge(ItemRequirement other) { return of(this.filter, this.count + other.count); }

    public static ItemRequirement of(Predicate<ItemStack> filter, int count) { return new ItemRequirement(filter, count); }
    public static ItemRequirement of(ItemStack stack) {
        if(stack == null || stack.isEmpty())
            return NULL;
        return of((s) -> InventoryUtil.ItemMatches(s, stack), stack.getCount());
    }
    public static ItemRequirement ofItemNoNBT(ItemStack stack) {
        if(stack == null || stack.isEmpty())
            return NULL;
        return of(stack.getItem(), stack.getCount());
    }
    public static ItemRequirement of(Item item, int count) {
        if(item == null || item == Items.AIR)
            return NULL;
        return of((s) -> s.getItem() == item, count);
    }

    /**
     * Randomly selects items matching the required inputs.
     * Used to randomly select an item to sell.
     * Returns null if no items within the container matched the requirements.
     * Does not actually remove the items from the container, this is merely a query function.
     */
    public static List<ItemStack> getRandomItemsMatchingRequirements(IItemHandler container, ItemRequirement requirement1, ItemRequirement requirement2)
    {
        if(requirement1.isNull() && requirement2.isNull())
            return null;
        if(requirement1.isNull())
        {
            List<ItemStack> validItems = getValidItems(container, requirement2);
            if(validItems.size() == 0)
                return null;
            return Lists.newArrayList(getRandomItem(validItems, requirement2.count));
        }
        else if(requirement2.isNull())
        {
            List<ItemStack> validItems = getValidItems(container, requirement1);
            if(validItems.size() == 0)
                return null;
            return Lists.newArrayList(getRandomItem(validItems, requirement1.count));
        }

        List<ItemStack> validItems1 = getValidItems(container, requirement1);
        List<ItemStack> validItems2 = getValidItems(container, requirement2);
        //Remove conflicts
        for(int x = 0; x < validItems1.size(); ++x)
        {
            ItemStack s1 = validItems1.get(x);
            for(int y = 0; y < validItems2.size(); ++y)
            {
                ItemStack s2 = validItems2.get(y);
                if(InventoryUtil.ItemMatches(s1, s2))
                {
                    int count = InventoryUtil.GetItemCount(container, s1);
                    //Check if we have enough of the item to fullfill both requirements (should they both select that item)
                    if(count < requirement1.count + requirement2.count)
                    {
                        //Need to remove it from one or the other
                        if(validItems2.size() == 1)
                        {
                            //Remove from the 1st list if the 2nd list only has 1 entry left
                            validItems1.remove(s1);
                            x--;
                        }
                        else
                        {
                            //Otherwise remove from the 2nd list by default
                            validItems2.remove(s2);
                            y--;
                        }
                    }
                }
            }
        }
        if(validItems1.size() > 0 && validItems2.size() > 0)
            return Lists.newArrayList(getRandomItem(validItems1, requirement1.count), getRandomItem(validItems2, requirement2.count));
        else
            return null;
    }

    public static List<ItemStack> getValidItems(IItemHandler container, ItemRequirement requirement)
    {
        List<ItemStack> validItems = new ArrayList<>();
        for(int i = 0; i < container.getSlots(); ++i)
        {
            ItemStack stack = container.getStackInSlot(i);
            if(requirement.test(stack) && InventoryUtil.GetItemCount(container, stack) >= requirement.count && isNotInList(validItems, stack))
                validItems.add(stack.copy());
        }
        return validItems;
    }

    public static boolean isNotInList(List<ItemStack> list, ItemStack stack)
    {
        for(ItemStack i : list)
        {
            if(InventoryUtil.ItemMatches(i, stack))
                return false;
        }
        return true;
    }

    public static ItemStack getRandomItem(List<ItemStack> validItems, int count) {
        if(validItems.size() == 0)
            return ItemStack.EMPTY;
        ItemStack stack = validItems.get(new Random().nextInt(validItems.size()));
        stack.setCount(count);
        return stack;
    }

    /**
     * Predictably selects items matching the required inputs.
     * Used to collect barter/purchase items from a customer such that we can properly predict where it will come from.
     * Returns null if not enough items within the container matched the requirements.
     * Does not actually remove the items from the container, this is merely a query function.
     */
    public static List<ItemStack> getFirstItemsMatchingRequirements(Container container, ItemRequirement... requirements)
    {
        List<ItemStack> results = new ArrayList<>();
        Map<Integer,Integer> consumedItems = new HashMap<>();
        for(ItemRequirement requirement : requirements)
        {
            int leftToConsume = requirement.count;
            for(int i = 0; i < container.getContainerSize() && leftToConsume > 0; ++i)
            {
                ItemStack stack = container.getItem(i);
                if(requirement.test(stack))
                {
                    int alreadyConsumed = consumedItems.getOrDefault(i,0);
                    int consumeCount = Math.min(leftToConsume, stack.getCount() - alreadyConsumed);
                    leftToConsume -= consumeCount;
                    if(consumeCount > 0)
                    {
                        consumedItems.put(i, alreadyConsumed + consumeCount);
                        //Search through results for another stack of the same item/nbt
                        boolean query = true;
                        for(int x = 0; x < results.size() && query; ++x)
                        {
                            if(InventoryUtil.ItemMatches(results.get(x), stack))
                            {
                                query = false;
                                results.get(x).grow(consumeCount);
                            }
                        }
                        if(query)
                        {
                            //None found in que
                            ItemStack result = stack.copy();
                            result.setCount(consumeCount);
                            results.add(result);
                        }
                    }
                }
            }
            if(leftToConsume > 0) //Requirement not met.
                return null;
        }
        return results;
    }

    /**
     * Predictably selects items matching the required inputs.
     * Used to collect barter/purchase items from a customer such that we can properly predict where it will come from.
     * Returns null if not enough items within the container matched the requirements.
     * Does not actually remove the items from the container, this is merely a query function.
     */
    public static List<ItemStack> getFirstItemsMatchingRequirements(IItemHandler container, ItemRequirement... requirements)
    {
        List<ItemStack> results = new ArrayList<>();
        Map<Integer,Integer> consumedItems = new HashMap<>();
        for(ItemRequirement requirement : requirements)
        {
            int leftToConsume = requirement.count;
            for(int i = 0; i < container.getSlots() && leftToConsume > 0; ++i)
            {
                ItemStack stack = container.getStackInSlot(i);
                if(requirement.test(stack))
                {
                    int alreadyConsumed = consumedItems.getOrDefault(i,0);
                    int consumeCount = Math.min(leftToConsume, stack.getCount() - alreadyConsumed);
                    leftToConsume -= consumeCount;
                    if(consumeCount > 0)
                    {
                        consumedItems.put(i, alreadyConsumed + consumeCount);
                        //Search through results for another stack of the same item/nbt
                        boolean query = true;
                        for(int x = 0; x < results.size() && query; ++x)
                        {
                            if(InventoryUtil.ItemMatches(results.get(x), stack))
                            {
                                query = false;
                                results.get(x).grow(consumeCount);
                            }
                        }
                        if(query)
                        {
                            //None found in que
                            ItemStack result = stack.copy();
                            result.setCount(consumeCount);
                            results.add(result);
                        }
                    }
                }
            }
            if(leftToConsume > 0) //Requirement not met.
                return null;
        }
        return results;
    }


}
