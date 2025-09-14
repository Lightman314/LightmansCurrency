package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.filter.FilterAPI;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketKioskRestriction;
import io.github.lightman314.lightmanscurrency.api.filter.IItemTradeFilter;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemTradeRestriction {

    public static final ResourceLocation NO_RESTRICTION_KEY = VersionUtil.lcResource("none");

    public static void init() {
        register(NO_RESTRICTION_KEY, NONE);
        register("equipment_head", EquipmentRestriction.HEAD);
        register("equipment_chest", EquipmentRestriction.CHEST);
        register("equipment_legs", EquipmentRestriction.LEGS);
        register("equipment_feet", EquipmentRestriction.FEET);
        register("ticket_kiosk", TicketKioskRestriction.REGISTERED_INSTANCE);
        register("book", BookRestriction.INSTANCE);
    }

    private static final Map<ResourceLocation,ItemTradeRestriction> registeredRestrictions = new HashMap<>();

    private static void register(String type, ItemTradeRestriction restriction) { register(VersionUtil.lcResource(type), restriction); }

    public static void register(ResourceLocation type, ItemTradeRestriction restriction) {
        if(registeredRestrictions.containsKey(type))
        {
            LightmansCurrency.LogWarning("Cannot register an Item Trade Restriction of type '" + type + "' as one is already registered.");
            return;
        }
        registeredRestrictions.put(type, restriction);
    }

    public static ResourceLocation getId(ItemTradeRestriction restriction) {
        if(restriction == null || !registeredRestrictions.containsValue(restriction))
            return NO_RESTRICTION_KEY;

        AtomicReference<ResourceLocation> result = new AtomicReference<>(NO_RESTRICTION_KEY);
        registeredRestrictions.forEach((type, r) -> { if(r == restriction) result.set(type); });
        return result.get();

    }

    public static void forEach(BiConsumer<ResourceLocation,ItemTradeRestriction> consumer) { registeredRestrictions.forEach(consumer); }

    public static final ItemTradeRestriction NONE = new ItemTradeRestriction();

    public ResourceLocation getType() { return getId(this); }

    protected ItemTradeRestriction() { }

    public boolean allowFilters() { return true; }

    public ItemStack modifySellItem(ItemStack sellItem, String customName, ItemTradeData trade, int index) { return sellItem; }

    public boolean displayCustomName(ItemStack sellItem, ItemTradeData trade, int index) { return true; }

    public Predicate<ItemStack> modifyFilter(Predicate<ItemStack> filter) { return filter; }

    public boolean allowSellItem(ItemStack itemStack) { return true; }

    public ItemStack filterSellItem(ItemStack itemStack) { return itemStack; }

    public boolean allowItemSelectItem(ItemStack itemStack) { return true; }

    public boolean allowExtraItemInStorage(ItemStack itemStack) { return false; }

    public int getSaleStock(TraderItemStorage traderStorage, ItemTradeData trade) {
        int minStock = Integer.MAX_VALUE;
        List<ItemRequirement> requirements = ItemRequirement.combineRequirements(trade.getItemRequirement(0),trade.getItemRequirement(1));
        if(requirements.isEmpty())
            return 0;
        ItemRequirement.MatchingItemsList matchingItems = ItemRequirement.getMatchingItems(traderStorage,requirements);
        for(int i = 0; i < requirements.size(); ++i)
            minStock = Math.min(matchingItems.getMatches(i) / requirements.get(i).getCount(),minStock);
        int dupeMatches = matchingItems.getDuplicateMatches();
        if(dupeMatches > 0)
        {
            //Round stock count down if there are dupes
            int totalCount = 0;
            for(ItemRequirement r : requirements)
                totalCount += r.getCount();
            int totalMatches = dupeMatches;
            for(int i = 0; i < requirements.size(); ++i)
                totalMatches += matchingItems.getUniqueMatches(i);
            minStock = Math.min(totalMatches / totalCount,minStock);
        }
        return minStock;
    }

    public List<ItemStack> getRandomSellItems(ItemTraderData trader, ItemTradeData trade)
    {
        List<ItemStack> randomItems = ItemRequirement.getRandomItemsMatchingRequirements(trader.getStorage(), trade.getItemRequirement(0), trade.getItemRequirement(1), trader.isCreative());
        if(randomItems == null && trader.isCreative()) //If creative, return nbt enforced version if no random items are present in the inventory.
        {
            randomItems = new ArrayList<>();
            for(int i = 0; i < 2; ++i)
            {
                ItemStack internal = trade.getActualItem(i);
                IItemTradeFilter filter = FilterAPI.tryGetFilter(internal);
                if(filter != null)
                {
                    //If a filter is present, return a random valid item from the item list
                    List<ItemStack> allItems = filter.getDisplayableItems(internal,null);
                    if(!allItems.isEmpty())
                    {
                        Random r = new Random();
                        for(int c = 0; c < internal.getCount(); ++c)
                        {
                            int random = r.nextInt(allItems.size());
                            randomItems.add(allItems.get(random).copyWithCount(1));
                        }
                    }
                }
                else
                {
                    ItemStack sellItem = trade.getSellItem(i);
                    if(!sellItem.isEmpty())
                        randomItems.add(sellItem);
                }
            }
            randomItems = InventoryUtil.combineQueryItems(randomItems);
        }
        return randomItems;
    }

    protected final int getItemStock(ItemStack sellItem, TraderItemStorage traderStorage)
    {
        if(sellItem.isEmpty())
            return Integer.MAX_VALUE;
        return traderStorage.getItemCount(sellItem) / sellItem.getCount();
    }

    public void removeItemsFromStorage(TraderItemStorage traderStorage, List<ItemStack> soldItems)
    {
        for(ItemStack sellItem : soldItems)
            this.removeFromStorage(sellItem, traderStorage);
    }

    protected final void removeFromStorage(ItemStack sellItem, TraderItemStorage traderStorage)
    {
        if(sellItem.isEmpty())
            return;
        traderStorage.removeItem(sellItem);
    }

    public boolean alwaysEnforceNBT(int tradeSlot) { return false; }

    @OnlyIn(Dist.CLIENT)
    public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG() { return EasySlot.BACKGROUND; }

}