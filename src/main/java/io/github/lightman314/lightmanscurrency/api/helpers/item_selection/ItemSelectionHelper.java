package io.github.lightman314.lightmanscurrency.api.helpers.item_selection;

import com.google.common.base.Predicates;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ItemSelectionHelper {

    private ItemSelectionHelper() {}

    private static List<ItemStack> allItems = List.of();
    private static Map<Identifier,List<ItemStack>> prefilteredItems = Map.of();
    private static final Map<Identifier,ItemSelectionFilter> registeredFilters = new HashMap<>();
    private static final List<Predicate<CreativeModeTab>> tabBlacklist = new ArrayList<>();
    private static final List<Predicate<ItemStack>> itemBlacklist = new ArrayList<>();
    private static boolean rebuilding = false;

    static {
        registerFilter(LCApi.id("null"),Predicates.alwaysTrue());
        blacklistCreativeTab(CreativeModeTabs.HOTBAR,CreativeModeTabs.INVENTORY,CreativeModeTabs.SEARCH,CreativeModeTabs.OP_BLOCKS);
    }

    public static List<ItemStack> getAllItems() { return allItems; }
    public static List<ItemStack> getFilteredItems(@Nullable ItemSelectionFilter filter) { return getFilteredItems(filter == null ? null : filter.key()); }
    public static List<ItemStack> getFilteredItems(@Nullable Identifier filter) {
        if(filter == null)
            return getAllItems();
        if(prefilteredItems.containsKey(filter))
            return prefilteredItems.get(filter);
        return List.of();
    }

    public static void registerFilter(Identifier key, Predicate<ItemStack> filter) { registerFilter(new ItemSelectionFilter(key,filter)); }
    public static void registerFilter(ItemSelectionFilter filter) {
        registeredFilters.put(filter.key(),filter);
    }

    public static void blacklistCreativeTab(CreativeModeTab... tabs) {
        for(CreativeModeTab tab : tabs)
            blacklistCreativeTab(t -> t == tab);
    }
    public static void blacklistCreativeTab(ResourceKey<CreativeModeTab>... tabs) {
        for(ResourceKey<CreativeModeTab> tab : tabs)
            blacklistCreativeTab(t -> BuiltInRegistries.CREATIVE_MODE_TAB.getValue(tab) == t);
    }
    public static void blacklistCreativeTab(Predicate<CreativeModeTab> filter) { tabBlacklist.add(filter); }


    public static void blacklistItem(Item item) { blacklistItem(s -> s.is(item)); }
    public static void blacklistItem(Holder<Item> item) { blacklistItem(s -> s.is(item)); }
    public static void blacklistItem(Supplier<? extends ItemLike> item) { blacklistItem(s -> s.is(item.get().asItem())); }
    public static void blacklistItem(Predicate<ItemStack> filter) {
        itemBlacklist.add(filter);
    }

    public static boolean isCreativeTabAllowed(CreativeModeTab tab) {
        for(Predicate<CreativeModeTab> filter : tabBlacklist) {
            if(filter.test(tab))
                return false;
        }
        return true;
    }

    public static boolean isItemAllowed(ItemStack stack) {
        for(Predicate<ItemStack> filter : itemBlacklist) {
            if(filter.test(stack))
                return false;
        }
        return true;
    }

    public static void assertDataIsLoaded() { assertDataIsLoaded(() -> {}); }
    public static void assertDataIsLoaded(Runnable listener) {
        if(rebuilding) //Abort if we're already rebuilding
            return;
        if(allItems.isEmpty())
            rebuilding = true;
        new Thread(() -> safeInitItemList(listener)).start();
    }

    private static void safeInitItemList(Runnable listener) {
        try {
            Client.initItemList(listener);
        } catch (Throwable t) { LightmansCurrency.LogError("Error occurred while attempting to set up the Item List!"); }
        rebuilding = false;
    }

    private static final class Client {
        private Client() {}
        private static void initItemList(Runnable listener) {
            Minecraft mc = Minecraft.getInstance();
            if(mc == null)
                return;

            LocalPlayer player = mc.player;
            if(player == null)
                return;


            FeatureFlagSet flagSet = player.connection.enabledFeatures();
            boolean hasPermissions = mc.options.operatorItemsTab().get() && player.canUseGameMasterBlocks();
            RegistryAccess registryAccess = player.registryAccess();

            //Force Creative Tab contents rebuild
            if(!CreativeModeTabs.tryRebuildTabContents(flagSet,hasPermissions,registryAccess) && !allItems.isEmpty()) {
                return;
            }

            LightmansCurrency.LogInfo("Pre-filtering item list for Item Selection items.");
            rebuilding = true;

            List<ItemStack> newList = new ArrayList<>();
            for(CreativeModeTab tab : CreativeModeTabs.allTabs()) {
                if(isCreativeTabAllowed(tab)) {
                    //Add all items in this creative tab to the list
                    //while also confirming that we don't already have it in the list
                    try{
                        for(ItemStack stack : tab.getDisplayItems()) {
                            if(isItemAllowed(stack)) {
                                addToList(newList,stack);
                                //Add all enchantment levels for enchanted books
                                if(stack.getItem() == Items.ENCHANTED_BOOK) {
                                    ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
                                    for(var entry : enchantments.entrySet()) {
                                        for(int newLevel = entry.getIntValue() - 1; newLevel > 0; newLevel--) {
                                            ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
                                            ItemEnchantments.Mutable e = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                                            e.set(entry.getKey(),newLevel);
                                            EnchantmentHelper.setEnchantments(newBook,e.toImmutable());
                                            if(isItemAllowed(newBook))
                                                addToList(newList,newBook);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Throwable t) { LightmansCurrency.LogError("Error getting display items from the '" + tab.getDisplayName().getString() + "' tab!");}
                }
            }

            Map<Identifier,List<ItemStack>> newFilteredItems = new HashMap<>();
            registeredFilters.forEach((key,filter) ->
                newFilteredItems.put(key,newList.stream().filter(filter).toList()));

            allItems = List.copyOf(newList);
            prefilteredItems = Map.copyOf(newFilteredItems);

            listener.run();

        }

        private static void addToList(List<ItemStack> list,ItemStack newStack) {
            for(ItemStack stack : list) {
                if(ItemStack.isSameItemSameComponents(stack,newStack))
                    return;
            }
            list.add(newStack);
        }

    }

}