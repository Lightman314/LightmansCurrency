package io.github.lightman314.lightmanscurrency.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ItemRequirement implements Predicate<ItemStack> {

    private static ItemRequirement NULL = null;
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
    public final boolean isValid() { return !this.isNull(); }

    public boolean tryMerge(ItemRequirement other)
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
    public static ItemRequirement fromFilter(ItemStack filterItem,Predicate<ItemStack> filter)
    {
        if(filter == null)
            return of(filterItem);
        return new FilterRequirement(filterItem,filter);
    }

    public static List<ItemRequirement> combineRequirements(ItemRequirement... requirements) { return combineRequirements(Lists.newArrayList(requirements)); }
    public static List<ItemRequirement> combineRequirements(List<ItemRequirement> requirements)
    {
        List<ItemRequirement> list = new ArrayList<>();
        for(ItemRequirement requirement : requirements)
        {
            if(requirement.isValid())
            {
                for (ItemRequirement r : list) {
                    if(r.tryMerge(requirement))
                        break;
                }
                if(requirement.isValid())
                    list.add(requirement);
            }
        }
        return list;
    }

    public static MatchingItemsList getMatchingItems(IItemHandler container, List<ItemRequirement> requirements)
    {
        MatchingItemsList list = new MatchingItemsList(requirements);
        list.testContainer(container);
        return list;
    }

    /**
     * Randomly selects items matching the required inputs.
     * Used to randomly select an item to sell.
     * Returns null if no items within the container matched the requirements.
     * Does not actually remove the items from the container, this is merely a query function.
     */
    @Nullable
    public static List<ItemStack> getRandomItemsMatchingRequirements(IItemHandler container, List<ItemRequirement> requirements, boolean creative)
    {
        requirements = combineRequirements(requirements);
        if(requirements.isEmpty())
            return null;
        MatchingItemsList matchingItems = getMatchingItems(container,requirements);
        return matchingItems.getRandomItems(!creative);
    }

    private static void addToList(List<ItemStack> list, ItemStack stack)
    {
        for(ItemStack s : list)
        {
            if(InventoryUtil.ItemMatches(s,stack))
            {
                s.grow(stack.getCount());
                return;
            }
        }
        list.add(stack.copy());
    }

    private static void removeFromList(List<ItemStack> list, ItemStack stack)
    {
        for(int i = 0; i < list.size(); ++i)
        {
            ItemStack s = list.get(i);
            if(InventoryUtil.ItemMatches(s,stack))
            {
                s.shrink(stack.getCount());
                if(s.isEmpty())
                    list.remove(i);
                return;
            }
        }
    }

    public static class MatchingItemsList
    {
        private final List<ItemRequirement> requirements;
        private final Map<Set<Integer>,List<ItemStack>> validItemsMap = new HashMap<>();

        public MatchingItemsList(List<ItemRequirement> requirements)
        {
            this.requirements = ImmutableList.copyOf(requirements);
        }

        public void testContainer(Container container)
        {
            for(int i = 0; i < container.getContainerSize(); ++i)
                this.testValidItem(container.getItem(i));
        }

        public void testContainer(IItemHandler container)
        {
            for(int i = 0; i < container.getSlots(); ++i)
                this.testValidItem(container.getStackInSlot(i));
        }

        public void testValidItem(ItemStack stack)
        {
            Set<Integer> matches = new HashSet<>();
            for(int i = 0; i < this.requirements.size(); ++i)
            {
                ItemRequirement r = this.requirements.get(i);
                if(r.test(stack))
                    matches.add(i);
            }
            List<ItemStack> list = this.validItemsMap.getOrDefault(matches,new ArrayList<>());
            addToList(list,stack);
            this.validItemsMap.put(matches,list);
        }

        public int getDuplicateMatches()
        {
            int count = 0;
            for(var key : this.validItemsMap.keySet())
            {
                if(key.size() > 1)
                    count += getTotalCount(this.validItemsMap.get(key));
            }
            return count;
        }

        public int getUniqueMatches(int requrementIndex)
        {
            for(var key : this.validItemsMap.keySet())
            {
                if(key.contains(requrementIndex) && key.size() == 1)
                    return getTotalCount(this.validItemsMap.get(key));
            }
            return 0;
        }

        public int getMatches(int requirementIndex)
        {
            int count = 0;
            for(var key : this.validItemsMap.keySet())
            {
                if(key.contains(requirementIndex))
                    count += getTotalCount(this.validItemsMap.get(key));
            }
            return count;
        }

        @Nullable
        public List<ItemStack> getRandomItems(boolean removeFromList)
        {
            List<RequirementEntry> data = RequirementEntry.create(this.requirements);
            //Confirm that each requirement has enough
            for(RequirementEntry entry : data)
            {
                int required = removeFromList ? entry.requirement.getCount() : 1;
                entry.totalMatches = this.getMatches(entry.index);
                if(entry.totalMatches < required)
                {
                    //LightmansCurrency.LogDebug("Requirement " + entry.index + " failed as only " + entry.totalMatches + " of " + required + " items were available");
                    return null;
                }
                //Check if the matches unique to it are enough
                int uniqueRequired = removeFromList ? required : 0;
                entry.uniqueMatches = this.getUniqueMatches(entry.index);
                if(entry.uniqueMatches < uniqueRequired)
                {
                    entry.requiredDupes = uniqueRequired - entry.uniqueMatches;
                    //LightmansCurrency.LogDebug("Requirement " + entry.index + " requires at least " + entry.requiredDupes + "dupe matches");
                }
            }
            //Avoid conflicts
            for(RequirementEntry entry : data)
            {
                if(entry.requiredDupes > 0)
                {
                    int dupesInvolvingThis = 0;
                    //Limit other requirements dupes to the amount
                    List<Set<Integer>> relevantKeys = new ArrayList<>();
                    for(var key : this.validItemsMap.keySet())
                    {
                        if(key.contains(entry.index) && key.size() > 1)
                        {
                            dupesInvolvingThis += getTotalCount(this.validItemsMap.get(key));
                            relevantKeys.add(key);
                        }
                    }
                    //Simply cannot even with dupes
                    if(dupesInvolvingThis < entry.requiredDupes)
                    {
                        //LightmansCurrency.LogDebug("Requirement " + entry.index + " failed as only " + dupesInvolvingThis + " of " + entry.requiredDupes + " were available!");
                        return null;
                    }
                    else
                    {
                        int flexibleDupes = dupesInvolvingThis - entry.requiredDupes;
                        for(var key : relevantKeys)
                        {
                            for(int index : key)
                            {
                                if(index == entry.index)
                                    continue;
                                RequirementEntry e = data.get(index);
                                e.dupeRestrictions.put(entry.index,flexibleDupes);
                                //LightmansCurrency.LogDebug("Requirement " + e.index + " is now only allowed to use " + flexibleDupes + " duplicate results with " + entry.index);
                                //No longer able to fulfill its required item count
                                int newTotal = e.totalDupeAllotment(data.size());
                                if(newTotal < e.requiredDupes)
                                {
                                    //LightmansCurrency.LogDebug("Requirement " + e.index + " failed because it is now only allowed to use " + newTotal + " dupes of the " + e.requiredDupes + " it requires.");
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
            //Start collecting items
            List<ItemStack> results = new ArrayList<>();
            for(RequirementEntry entry : data)
            {
                int count = entry.requirement.getCount();
                while(count-- > 0)
                {
                    List<Pair<Set<Integer>,List<ItemStack>>> validEntries = new ArrayList<>();
                    //LightmansCurrency.LogDebug("Requirement " + entry.index + " has " + this.validItemsMap.size() + " possible sets to check for matches and conflicts with.");
                    for(var key : this.validItemsMap.keySet())
                    {
                        if(key.contains(entry.index))
                        {
                            boolean allowed = true;
                            for(int other : key)
                            {
                                if(!entry.allowedConflict(other))
                                {
                                    //LightmansCurrency.LogDebug("Requirement " + entry.index);
                                    allowed = false;
                                }

                            }
                            if(allowed)
                                validEntries.add(Pair.of(key,this.validItemsMap.get(key)));
                        }
                    }
                    //LightmansCurrency.LogDebug("Requirement " + entry.index + " has " + validEntries.size() + " entry sets to randomize results from.");
                    Pair<Set<Integer>,List<ItemStack>> set = ListUtil.weightedRandomItemFromList(validEntries,p -> getTotalCount(p.getSecond()));
                    if(set != null)
                    {
                        //LightmansCurrency.LogDebug("Requirement " + entry.index + " has " + set.getSecond().size() + " items in the list to randomize results from.");
                        ItemStack item = ListUtil.weightedRandomItemFromList(set.getSecond(),ItemStack::getCount);
                        if(item == null)
                        {
                            //LightmansCurrency.LogDebug("Requirement " + entry.index + " failed because no random item was found from the list");
                            return null;
                        }

                        //Add the item to the results
                        item = item.copyWithCount(1);
                        addToList(results,item);
                        //Remove the item from the list of available items
                        if(removeFromList)
                        {
                            removeFromList(set.getSecond(),item);
                            //Remove the list from the map if the data is empty
                            if(set.getSecond().isEmpty())
                                this.validItemsMap.remove(set.getFirst());
                        }
                        //Let itself know if any dupe restrctions should be consumed
                        entry.afterDupeConsumption(set.getFirst());
                    }
                    else
                    {
                        //LightmansCurrency.LogDebug("Requirement " + entry.index + " failed because no more valid sets were found");
                        return null;
                    }
                }
            }
            return results;
        }

        private static class RequirementEntry
        {
            final ItemRequirement requirement;
            final int index;
            private RequirementEntry(ItemRequirement requirement,int index) { this.requirement = requirement; this.index = index; }
            Map<Integer,Integer> dupeRestrictions = new HashMap<>();
            private boolean allowedConflict(int otherIndex) { return otherIndex == this.index || this.getRestriction(otherIndex) > 0; }
            private int getRestriction(int otherIndex) { return this.dupeRestrictions.getOrDefault(otherIndex,Integer.MAX_VALUE); }
            private void afterDupeConsumption(Set<Integer> key)
            {
                for(int index : key)
                {
                    if(index != this.index && this.dupeRestrictions.containsKey(index))
                        this.dupeRestrictions.put(index,this.dupeRestrictions.get(index) - 1);
                }
            }
            private int totalDupeAllotment(int totalSize)
            {
                int count = 0;
                for(int i = 0; i < totalSize; ++i)
                {
                    if(i == this.index)
                        continue;
                    if(this.dupeRestrictions.containsKey(i))
                        count += this.dupeRestrictions.get(i);
                    else
                        return Integer.MAX_VALUE;
                }
                return count;
            }
            int requiredDupes = 0;
            int totalMatches = 0;
            int uniqueMatches = 0;

            static List<RequirementEntry> create(List<ItemRequirement> list)
            {
                List<RequirementEntry> result = new ArrayList<>();
                for(int i = 0; i < list.size(); ++i)
                    result.add(new RequirementEntry(list.get(i),i));
                return ImmutableList.copyOf(result);
            }
        }

    }

    private static int getTotalCount(List<ItemStack> list)
    {
        int count = 0;
        for(ItemStack s : list)
            count += s.getCount();
        return count;
    }

    @Nullable
    public static List<ItemStack> getRandomItems(List<ItemStack> validItems, int count, boolean creative) {
        if(validItems.isEmpty())
            return new ArrayList<>();
        List<ItemStack> result = new ArrayList<>();
        for(int i = 0; i < count; ++i)
        {
            ItemStack randomStack = ListUtil.weightedRandomItemFromList(validItems,ItemStack::getCount);
            if(randomStack != null)
            {
                ItemStack removed = randomStack.copyWithCount(1);
                if(!creative)
                    removeFromList(validItems,removed);
                result.add(removed);
            }
            else //If we ran out of items to collect, return an empty list
                return null;
        }
        return result;
    }

    /**
     * Predictably selects items matching the required inputs.
     * Used to collect barter/purchase items from a customer such that we can properly predict where it will come from.
     * Returns null if not enough items within the container matched the requirements.
     * Does not actually remove the items from the container, this is merely a query function.
     */
    @Nullable
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
    @Nullable
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

    public abstract boolean matches(ItemRequirement otherRequirement);

    private static class StackMatch extends ItemRequirement
    {
        private final ItemStack stack;
        private StackMatch(ItemStack stack) { this(stack, stack.getCount()); }
        private StackMatch(ItemStack stack, int count) { super(count); this.stack = stack; }

        @Override
        public boolean test(ItemStack stack) { return InventoryUtil.ItemMatches(this.stack,stack); }

        @Override
        public boolean matches(ItemRequirement otherRequirement) {
            if(otherRequirement instanceof StackMatch pm)
                return InventoryUtil.ItemMatches(this.stack,pm.stack);
            return false;
        }
    }

    private static class ItemMatch extends ItemRequirement
    {

        private final Item item;
        private ItemMatch(Item item, int count) { super(count); this.item = item; }

        @Override
        public boolean test(ItemStack stack) { return stack.getItem() == this.item; }

        @Override
        public boolean matches(ItemRequirement otherRequirement) {
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
        public boolean matches(ItemRequirement otherRequirement) {
            return otherRequirement instanceof IngredientRequirement other && other.ingredient.equals(this.ingredient);
        }
    }

    private static class FilterRequirement extends ItemRequirement
    {
        private final ItemStack filterItem;
        private final Predicate<ItemStack> filter;
        private FilterRequirement(ItemStack filterItem,Predicate<ItemStack> filter) { super(filterItem.getCount()); this.filter = filter; this.filterItem = filterItem.copy(); }
        @Override
        public boolean test(ItemStack stack) { return this.filter.test(stack); }
        @Override
        public boolean matches(ItemRequirement otherRequirement) {
            if(otherRequirement instanceof FilterRequirement fr)
                return InventoryUtil.ItemMatches(fr.filterItem,this.filterItem);
            return false;
        }
    }

    private static class NullRequirement extends ItemRequirement
    {
        private NullRequirement() { super(0); }
        @Override
        public boolean test(ItemStack stack) { return false; }
        @Override
        public boolean matches(ItemRequirement otherRequirement) { return otherRequirement instanceof NullRequirement; }
    }

}
