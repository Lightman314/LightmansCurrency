package io.github.lightman314.lightmanscurrency.common.traders.slot_machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.TraderItemHandler;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.slot_machine.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine.SlotMachineEntryTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine.SlotMachinePriceTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine.SlotMachineStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.SlotMachineTradeNotification;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.SlotMachineTrade;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlotMachineTraderData extends InputTraderData implements TraderItemStorage.ITraderItemFilter, TraderItemHandler.IItemStorageProvider {

    public static final TraderType<SlotMachineTraderData> TYPE = new TraderType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "slot_machine_trader"),SlotMachineTraderData::new);

    TraderItemHandler<SlotMachineTraderData> itemHandler = new TraderItemHandler<>(this);

    public IItemHandler getItemHandler(Direction relativeSide) { return this.itemHandler.getHandler(relativeSide); }

    private MoneyValue price = MoneyValue.empty();
    public final MoneyValue getPrice() { return this.price; }
    public void setPrice(MoneyValue newValue) { this.price = newValue; this.markPriceDirty(); }
    public final boolean isPriceValid() { return this.price.isFree() || !this.price.isEmpty(); }

    private List<ItemStack> lastReward = new ArrayList<>();
    public List<ItemStack> getLastRewards() { return ImmutableList.copyOf(this.lastReward); }

    private final List<SlotMachineEntry> entries = Lists.newArrayList(SlotMachineEntry.create());
    public final List<SlotMachineEntry> getAllEntries() { return new ArrayList<>(this.entries); }
    public final List<SlotMachineEntry> getValidEntries() { return this.entries.stream().filter(SlotMachineEntry::isValid).toList(); }
    private boolean entriesChanged = false;
    public boolean areEntriesChanged() { return this.entriesChanged; }
    public void clearEntriesChangedCache() { this.entriesChanged = false; }
    public void addEntry() { if(this.entries.size() >= TraderData.GLOBAL_TRADE_LIMIT) return; this.entries.add(SlotMachineEntry.create()); this.markEntriesDirty(); }
    public void removeEntry(int entryIndex) {
        if(entryIndex < 0 || entryIndex >= this.entries.size())
            return;
        this.entries.remove(entryIndex);
        this.markEntriesDirty();
    }
    public final int getTotalWeight() {
        int weight = 0;
        for(SlotMachineEntry entry : this.getValidEntries())
            weight += entry.getWeight();
        return weight;
    }

    @Nullable
    public final SlotMachineEntry getRandomizedEntry(TradeContext context)
    {
        Level level;
        if(context.hasPlayer())
            level = context.getPlayer().level();
        else
        {
            try{ level = LightmansCurrency.getProxy().safeGetDummyLevel();
            } catch(Throwable t) {
                LightmansCurrency.LogError("Could not get a valid level from the trade's context or the proxy. Will have to use Java randomizer");
                return this.getRandomizedEntry(new Random().nextInt(this.getTotalWeight()) + 1);
            }
        }
        return this.getRandomizedEntry(level.random.nextInt(this.getTotalWeight()) + 1);
    }

    private SlotMachineEntry getRandomizedEntry(int rand)
    {
        for(SlotMachineEntry entry : this.getValidEntries())
        {
            rand -= entry.getWeight();
            if(rand <= 0)
                return entry;
        }
        return null;
    }

    @Nonnull
    public final List<Component> getSlotMachineInfo()
    {
        List<Component> tooltips = new ArrayList<>();
        //Return undefined info if not yet defined
        if(!this.hasValidTrade())
        {
            tooltips.add(LCText.TOOLTIP_SLOT_MACHINE_UNDEFINED.get().withStyle(ChatFormatting.RED));
            return tooltips;
        }

        if(!this.hasStock())
            tooltips.add(LCText.TOOLTIP_OUT_OF_STOCK.get().withStyle(ChatFormatting.RED));

        return tooltips;
    }

    public String getOdds(int weight)
    {
        DecimalFormat df = new DecimalFormat();
        double odds = ((double)weight/(double)this.getTotalWeight()) * 100d;
        df.setMaximumFractionDigits(odds < 1d ? 2 : 0);
        return df.format(odds);
    }

    private final TraderItemStorage storage = new TraderItemStorage(this);
    @Nonnull
    public final TraderItemStorage getStorage() { return this.storage; }

    private SlotMachineTraderData() { super(TYPE); }
    public SlotMachineTraderData(@Nonnull Level level, @Nonnull BlockPos pos) { super(TYPE, level, pos); }

    private final ImmutableList<SlotMachineTrade> trade = ImmutableList.of(new SlotMachineTrade(this));

    @Override
    public IconData inputSettingsTabIcon() { return IconData.of(Items.HOPPER); }

    @Override
    public MutableComponent inputSettingsTabTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_INPUT_ITEM.get(); }

    @Override
    public IconData getIcon() { return IconUtil.ICON_TRADER_ALT; }

    @Override
    protected boolean allowAdditionalUpgradeType(UpgradeType type) { return type == Upgrades.ITEM_CAPACITY; }

    @Override
    public int getTradeCount() { return 1; }

    @Override
    public int getTradeStock(int tradeIndex) {
        if(!this.hasValidTrade())
            return 0;
        if(this.isCreative())
            return 1;
        int minStock = Integer.MAX_VALUE;
        for(SlotMachineEntry entry : this.entries)
        {
            int stock = entry.getStock(this);
            if(stock < minStock)
                minStock = stock;
        }
        return minStock;
    }

    public boolean hasStock() { return this.getTradeStock(0) > 0; }

    @Override
    public boolean hasValidTrade() { return this.entries.stream().anyMatch(SlotMachineEntry::isValid) && this.isPriceValid(); }

    @Override
    protected void saveTrades(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) { }

    @Override
    protected MenuProvider getTraderMenuProvider(@Nonnull MenuValidator validator) { return new SlotMachineMenuProvider(this.getID(), validator); }

    private record SlotMachineMenuProvider(long traderID, @Nonnull MenuValidator validator) implements EasyMenuProvider {

        @Override
        public AbstractContainerMenu createMenu(int windowID, @Nonnull Inventory inventory, @Nonnull Player player) { return new SlotMachineMenu(windowID, inventory, this.traderID, this.validator); }

    }

    public final void markStorageDirty() { this.markDirty(this::saveStorage); }
    public final void markLastRewardDirty() { this.markDirty(this::saveLastRewards); }
    public final void markEntriesDirty() { this.markDirty(this::saveEntries); }
    public final void markPriceDirty() { this.markDirty(this::savePrice); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        super.saveAdditional(compound,lookup);
        this.saveStorage(compound,lookup);
        this.saveLastRewards(compound,lookup);
        this.saveEntries(compound,lookup);
        this.savePrice(compound);
    }

    protected final void saveStorage(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) { this.storage.save(compound,"Storage",lookup); }

    protected final void saveLastRewards(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        ListTag itemList = new ListTag();
        for(ItemStack reward : this.lastReward)
        {
            if(reward.isEmpty())
                continue;
            itemList.add(InventoryUtil.saveItemNoLimits(reward,lookup));
        }
        compound.put("LastReward", itemList);
    }

    protected final void saveEntries(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for(SlotMachineEntry entry : this.entries)
            list.add(entry.save(lookup));
        compound.put("Entries", list);
    }

    protected final void savePrice(CompoundTag compound) { compound.put("Price", this.price.save()); }

    @Override
    protected void loadAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        super.loadAdditional(compound,lookup);
        if(compound.contains("Storage"))
            this.storage.load(compound, "Storage", lookup);
        if(compound.contains("LastReward"))
        {
            this.lastReward.clear();
            ListTag itemList = compound.getList("LastReward", Tag.TAG_COMPOUND);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = InventoryUtil.loadItemNoLimits(itemList.getCompound(i),lookup);
                if(!stack.isEmpty())
                    this.lastReward.add(stack);
            }
        }
        if(compound.contains("Entries"))
        {
            this.entries.clear();
            ListTag list = compound.getList("Entries", Tag.TAG_COMPOUND);
            for(int i = 0; i < list.size(); ++i)
                this.entries.add(SlotMachineEntry.load(list.getCompound(i),lookup));
            this.entriesChanged = true;
        }
        if(compound.contains("Price"))
            this.price = MoneyValue.safeLoad(compound, "Price");
    }

    @Override
    protected void saveAdditionalToJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) {
        //Price
        json.add("Price", this.price.toJson());
        //Entries
        JsonArray entryList = new JsonArray();
        for(SlotMachineEntry entry : this.entries)
        {
            if(entry.isValid())
                entryList.add(entry.toJson(lookup));
        }
        json.add("Entries", entryList);
    }

    @Override
    protected void loadAdditionalFromJson(JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {

        if(json.has("Price"))
            this.price = MoneyValue.loadFromJson(json.get("Price"));
        else
            throw new JsonSyntaxException("Expected a 'Price' entry!");

        this.entries.clear();
        JsonArray entryList = GsonHelper.getAsJsonArray(json, "Entries");
        for(int i = 0; i < entryList.size(); ++i)
        {
            try{
                this.entries.add(SlotMachineEntry.parse(GsonHelper.convertToJsonObject(entryList.get(i), "Entries[" + i + "]"),lookup));
            } catch(JsonSyntaxException | ResourceLocationException t) { LightmansCurrency.LogError("Error parsing Slot Machine Trader Entry #" + (i + 1), t); }
        }
        if(this.entries.isEmpty())
            throw new JsonSyntaxException("Slot Machine Trader had no valid Entries!");

    }

    //No need for persistent data
    @Override
    protected void saveAdditionalPersistentData(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        this.saveLastRewards(compound,lookup);
    }

    @Override
    protected void loadAdditionalPersistentData(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        if(compound.contains("LastReward"))
        {
            this.lastReward = new ArrayList<>();
            ListTag itemList = compound.getList("LastReward", Tag.TAG_COMPOUND);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = InventoryUtil.loadItemNoLimits(itemList.getCompound(i),lookup);
                if(!stack.isEmpty())
                    this.lastReward.add(stack);
            }
        }
    }

    @Override
    protected void getAdditionalContents(List<ItemStack> results) { results.addAll(this.storage.getSplitContents()); }

    @Nonnull
    @Override
    public List<SlotMachineTrade> getTradeData() { return this.trade; }

    @Nullable
    @Override
    public SlotMachineTrade getTrade(int tradeIndex) { return this.trade.getFirst(); }

    //Trades are not added/removed like other traders
    @Override
    public void addTrade(Player requestor) {}
    @Override
    public void removeTrade(Player requestor) {}

    @Override
    public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {

        if(!this.hasValidTrade())
            return TradeResult.FAIL_INVALID_TRADE;

        SlotMachineTrade trade = this.trade.getFirst();
        if(trade == null)
        {
            LightmansCurrency.LogError("Slot Machine somehow doesn't have a valid trade!");
            return TradeResult.FAIL_INVALID_TRADE;
        }

        if(!context.hasPlayerReference())
            return TradeResult.FAIL_NULL;

        if(!this.hasStock())
            return TradeResult.FAIL_OUT_OF_STOCK;

        //Check if the player is allowed to do the trade
        if(this.runPreTradeEvent(trade, context).isCanceled())
            return TradeResult.FAIL_TRADE_RULE_DENIAL;

        //Get the cost of the trade
        MoneyValue price = this.runTradeCostEvent(trade, context).getCostResult();

        //Get the Result Items
        SlotMachineEntry loot = this.getRandomizedEntry(context);
        if(loot == null)
        {
            LightmansCurrency.LogError("Slot Machine encountered an error randomizing the loot.");
            return TradeResult.FAIL_NULL;
        }

        //Confirm that the customer can hold the rewards
        if(!loot.CanGiveToCustomer(context))
            return TradeResult.FAIL_NO_OUTPUT_SPACE;

        //Accept the payment
        if(context.getPayment(price))
        {
            if(!loot.GiveToCustomer(this, context))
            {
                //Refund the money taken
                context.givePayment(price);
                return TradeResult.FAIL_NO_OUTPUT_SPACE;
            }

            this.lastReward = loot.getDisplayItems();
            this.markLastRewardDirty();

            MoneyValue taxesPaid = MoneyValue.empty();

            //Ignore editing internal storage if this is flagged as creative.
            if(!this.isCreative())
            {
                //Give the paid cost to storage
                taxesPaid = this.addStoredMoney(price, true);

                //Push out of stock notification
                if(!this.hasStock())
                    this.pushNotification(OutOfStockNotification.create(this.getNotificationCategory(), -1));
            }

            //Handle Stats
            this.incrementStat(StatKeys.Traders.MONEY_EARNED,price);
            if(!taxesPaid.isEmpty())
                this.incrementStat(StatKeys.Taxables.TAXES_PAID,taxesPaid);
            if(loot.isMoney())
                this.incrementStat(StatKeys.Traders.MONEY_PAID, loot.getMoneyValue());

            //Push Notification
            this.pushNotification(SlotMachineTradeNotification.create(loot, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));

            //Push the post-trade event
            this.runPostTradeEvent(trade, context, price, taxesPaid);

            return TradeResult.SUCCESS;

        }
        else
            return TradeResult.FAIL_CANNOT_AFFORD;
    }

    @Override
    public boolean canMakePersistent() { return true; }

    @Override
    public void initStorageTabs(@Nonnull ITraderStorageMenu menu) {

        //Set basic tab to Entry Edit Tab
        menu.setTab(TraderStorageTab.TAB_TRADE_BASIC, new SlotMachineEntryTab(menu));
        //Price tab
        menu.setTab(1, new SlotMachinePriceTab(menu));
        //Storage Tab
        menu.setTab(2, new SlotMachineStorageTab(menu));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addPermissionOptions(List<PermissionOption> options) { }

    @Override
    public boolean isItemRelevant(ItemStack item) {
        for(SlotMachineEntry entry : this.entries)
        {
            if(entry.isItemRelevant(item))
                return true;
        }
        return false;
    }

    @Override
    public boolean allowExtraction(@Nonnull ItemStack stack) { return !this.isItemRelevant(stack); }

    @Override
    public int getStorageStackLimit() {
        int limit = ItemTraderData.DEFAULT_STACK_LIMIT;
        for(int i = 0; i < this.getUpgrades().getContainerSize(); ++i)
        {
            ItemStack stack = this.getUpgrades().getItem(i);
            if(stack.getItem() instanceof UpgradeItem upgradeItem)
            {
                if(this.allowUpgrade(upgradeItem) && upgradeItem.getUpgradeType() == Upgrades.ITEM_CAPACITY)
                    limit += UpgradeItem.getUpgradeData(stack).getIntValue(CapacityUpgrade.CAPACITY);
            }
        }
        return limit;
    }



}
