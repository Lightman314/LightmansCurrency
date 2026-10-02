package io.github.lightman314.lightmanscurrency.api.helpers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.mixin.ItemStackAccessor;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import javax.annotation.Nullable;
import java.util.*;

public final class ItemHelper {

    private ItemHelper() {}

    public static final ItemStackTemplate ALEX_HEAD;

    static {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        Multimap<String,Property> map = HashMultimap.create();
        map.put("textures",new Property("textures","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNiMDk4OTY3MzQwZGFhYzUyOTI5M2MyNGUwNDkxMDUwOWIyMDhlN2I5NDU2M2MzZWYzMWRlYzdiMzc1MCJ9fX0="));
        builder.set(DataComponents.PROFILE,ResolvableProfile.createResolved(new GameProfile(UUIDUtil.uuidFromIntArray(new int[] {-731408145, -304985227, -1778597514, 158507129 }),"",new PropertyMap(map))));
        ALEX_HEAD = new ItemStackTemplate(Items.PLAYER_HEAD,builder.build());
    }

    public static final TextEntry NOTIFICATION_ITEM_FORMAT = TextEntry.notification(LCApi.id("items"),"format");

    //Cache Skulls for convenience
    private static final Map<String,ItemStackTemplate> skullsByName = new HashMap<>();
    private static final Map<UUID,ItemStackTemplate> skullsById = new HashMap<>();

    public static List<ItemStack> ofSize(int size) {
        List<ItemStack> list = new ArrayList<>();
        while(list.size() < size)
            list.add(ItemStack.EMPTY);
        return list;
    }

    public static void validateList(List<ItemStack> list) {
        list.replaceAll(stack -> {
            if(ItemStackAccessor.validateComponents(stack.getComponents()).isError())
                return ItemStack.EMPTY;
            return stack;
        });
    }

    public static List<ItemStack> copyList(List<ItemStack> list) { return ListHelper.copyList(list,ItemStack::copy); }

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
        while(s.getCount() > s.getMaxStackSize())
            list.add(s.split(s.getMaxStackSize()));
        if(!s.isEmpty())
            list.add(s);
        return list;
    }

    public static int getItemIndex(Container container,ItemStack stack) {
        for(int i = 0; i < container.getContainerSize(); ++i)
        {
            if(container.getItem(i) == stack)
                return i;
        }
        return -1;
    }

    public static ItemStackTemplate skullForPlayer(String playerName)
    {
        if(!skullsByName.containsKey(playerName))
        {
            DataComponentPatch patch = DataComponentPatch.builder()
                            .set(DataComponents.PROFILE,ResolvableProfile.createUnresolved(playerName))
                            .build();
            ItemStackTemplate template = new ItemStackTemplate(Items.PLAYER_HEAD,1,patch);
            skullsByName.put(playerName,template);
        }
        return skullsByName.get(playerName);
    }

    public static ItemStackTemplate skullForPlayer(UUID playerID)
    {
        if(!skullsById.containsKey(playerID))
        {
            DataComponentPatch patch = DataComponentPatch.builder()
                    .set(DataComponents.PROFILE,ResolvableProfile.createUnresolved(playerID))
                    .build();
            ItemStackTemplate template = new ItemStackTemplate(Items.PLAYER_HEAD,1,patch);
            skullsById.put(playerID,template);
        }
        return skullsById.get(playerID);
    }

    @Nullable
    public static String getBigStackText(ItemStack stack) {
        if(stack.getCount() >= 1000)
            return (stack.getCount() / 1000) + "k";
        return null;
    }

    public static Component formatItemNames(List<ItemStack> items) {
        Component text = Component.literal("NULL");
        boolean first = true;
        for(ItemStack stack : items) {
            if(first) {
                text = formatItem(stack);
                first = false;
            } else {
                text = LCText.GENERIC_AND.get(text,formatItem(stack));
            }
        }
        return text;
    }

    public static Component formatItem(ItemStack item) {
        return item.getCount() <= 1 ? item.getHoverName() : NOTIFICATION_ITEM_FORMAT.get(item.getCount(),item.getHoverName());
    }

    public static int hashList(List<ItemStack> list) {
        List<Integer> hashes = new ArrayList<>();
        for(ItemStack s : list)
            hashes.add(ItemStack.hashItemAndComponents(s));
        return hashes.hashCode();
    }

    public static ItemStackTemplate asTemplate(ItemStack stack) { return new ItemStackTemplate(stack.getItem(),stack.getCount(),stack.getComponentsPatch()); }

}
