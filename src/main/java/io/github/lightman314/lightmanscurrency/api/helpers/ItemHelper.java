package io.github.lightman314.lightmanscurrency.api.helpers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import javax.annotation.Nullable;
import java.util.*;

public final class ItemHelper {

    private ItemHelper() {}

    //Cache Skulls for convenience
    private static final Map<String,ItemStack> skullsByName = new HashMap<>();
    private static final Map<UUID,ItemStack> skullsById = new HashMap<>();

    public static List<ItemStack> copyList(List<ItemStack> list)
    {
        List<ItemStack> copy = new ArrayList<>();
        for(ItemStack i : list)
            copy.add(i.copy());
        return copy;
    }

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
                    stack.grow(s2.getCount());
                    list.remove(i--);
                }
            }
            results.add(stack);
        }
        return results;
    }

    public static ItemStack safeGetStack(List<ItemStack> list,int index)
    {
        if(index < 0 || index >= list.size())
            return ItemStack.EMPTY;
        return list.get(index);
    }

    public static boolean listsMatch(List<ItemStack> list1,List<ItemStack> list2)
    {
        if(list1.size() != list2.size())
            return false;
        for(int i = 0; i < list1.size(); ++i)
        {
            ItemStack s1 = list1.get(i);
            ItemStack s2 = list2.get(i);
            if(!ItemStack.matches(s1,s2))
                return false;
        }
        return true;
    }

    public static List<ItemStack> splitStack(ItemStack stack)
    {
        ItemStack s = stack.copy();
        List<ItemStack> list = new ArrayList<>();
        while(stack.getCount() > s.getMaxStackSize())
            list.add(s.split(s.getMaxStackSize()));
        if(!s.isEmpty())
            list.add(s);
        return list;
    }

    public static ItemStack skullForPlayer(String playerName)
    {
        if(!skullsByName.containsKey(playerName))
        {
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(playerName));
            skullsByName.put(playerName,stack);
        }
        return skullsByName.get(playerName);
    }

    public static ItemStack skullForPlayer(UUID playerID)
    {
        if(!skullsById.containsKey(playerID))
        {
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            stack.set(DataComponents.PROFILE,ResolvableProfile.createUnresolved(playerID));
            skullsById.put(playerID,stack);
        }
        return skullsById.get(playerID);
    }

    @Nullable
    public static String getBigStackText(ItemStack stack) {
        if(stack.getCount() >= 1000)
            return (stack.getCount() / 1000) + "k";
        return null;
    }

}
