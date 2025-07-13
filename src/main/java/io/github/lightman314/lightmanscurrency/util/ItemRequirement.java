package io.github.lightman314.lightmanscurrency.util;

import com.google.common.collect.Lists;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

public abstract class ItemRequirement implements Predicate<ItemStack> {

    private static ItemRequirement NULL = null;
    @Nonnull
    public static ItemRequirement getNull() {
        if(NULL == null)
            NULL = new NullRequirement();
        return NULL;
    }

    private int count;
    public int getCount() { return this.count; }
    private ItemRequirement(int count) { this.count = count; }
    @Override
    public abstract boolean test(ItemStack stack);
    public final boolean isNull() { return this instanceof NullRequirement || this.count <= 0; }

    public boolean tryMerge(@Nonnull ItemRequirement other)
    {
        if(this.matches(other))
        {
            this.count += other.count;
            other.count = 0;
            return true;
        }
        return false;
    }

    public static ItemRequirement of(ItemStack stack) {
        if(stack == null || stack.isEmpty())
            return getNull();
        return new StackMatch(stack, stack.getCount());
    }
    public static ItemRequirement ofItemNoNBT(ItemStack stack) {
        if(stack == null || stack.isEmpty())
            return getNull();
        return of(stack.getItem(), stack.getCount());
    }
    public static ItemRequirement of(Item item, int count) {
        if(item == null || item == Items.AIR)
            return getNull();
        return new ItemMatch(item,count);
    }
    public static ItemRequirement of(Ingredient ingredient, int count) {
        if(ingredient == null)
            return getNull();
        return new IngredientRequirement(ingredient,count);
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
            if(validItems.isEmpty())
                return null;
            return Lists.newArrayList(getRandomItem(validItems, requirement2.count));
        }
        else if(requirement2.isNull())
        {
            List<ItemStack> validItems = getValidItems(container, requirement1);
            if(validItems.isEmpty())
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
        if(!validItems1.isEmpty() && !validItems2.isEmpty())
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
        if(validItems.isEmpty())
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

    public abstract boolean matches(@Nonnull ItemRequirement otherRequirement);

    private static class StackMatch extends ItemRequirement
    {
        private final ItemStack stack;
        private StackMatch(@Nonnull ItemStack stack) { this(stack, stack.getCount()); }
        private StackMatch(@Nonnull ItemStack stack, int count) { super(count); this.stack = stack; }

        @Override
        public boolean test(ItemStack stack) { return InventoryUtil.ItemMatches(this.stack,stack); }

        @Override
        public boolean matches(@Nonnull ItemRequirement otherRequirement) {
            if(otherRequirement instanceof StackMatch pm)
                return InventoryUtil.ItemMatches(this.stack,pm.stack);
            return false;
        }
    }

    private static class ItemMatch extends ItemRequirement
    {

        private final Item item;
        private ItemMatch(@Nonnull Item item, int count) { super(count); this.item = item; }

        @Override
        public boolean test(ItemStack stack) { return stack.getItem() == this.item; }

        @Override
        public boolean matches(@Nonnull ItemRequirement otherRequirement) {
            if(otherRequirement instanceof ItemMatch im)
                return im.item == this.item;
            return false;
        }

    }

    private static class IngredientRequirement extends ItemRequirement
    {
        private final Ingredient ingredient;
        private IngredientRequirement(Ingredient ingredient, int count) { super(count); this.ingredient = ingredient; }
        @Override
        public boolean test(ItemStack stack) { return this.ingredient.test(stack); }

        @Override
        public boolean matches(@Nonnull ItemRequirement otherRequirement) {
            return otherRequirement instanceof IngredientRequirement other && other.ingredient.equals(this.ingredient);
        }
    }

    private static class NullRequirement extends ItemRequirement
    {
        private NullRequirement() { super(0); }
        @Override
        public boolean test(ItemStack stack) { return false; }
        @Override
        public boolean matches(@Nonnull ItemRequirement otherRequirement) { return otherRequirement instanceof NullRequirement; }
    }

}