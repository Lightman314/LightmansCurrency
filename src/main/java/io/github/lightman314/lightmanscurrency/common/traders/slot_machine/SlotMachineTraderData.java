package io.github.lightman314.lightmanscurrency.common.traders.slot_machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine.SlotMachineEntryTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine.SlotMachinePriceTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine.SlotMachineStorageTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.SlotMachineTradeNotification;
import io.github.lightman314.lightmanscurrency.common.traders.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.SlotMachineTrade;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlotMachineTraderData extends TraderData implements TraderItemStorage.ITraderItemFilter {

    public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "slot_machine_trader");

    private CoinValue price = new CoinValue();
    public final CoinValue getPrice() { return this.price; }
    public void setPrice(CoinValue newValue) { this.price = newValue; this.markPriceDirty(); }
    public final boolean isPriceValid() { return this.price.isValid(); }

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
            level = context.getPlayer().level;
        else
        {
            try{ level = LightmansCurrency.PROXY.safeGetDummyLevel();
            } catch(Exception e) {
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

    public final List<Component> getSlotMachineInfo()
    {
        List<Component> tooltips = new ArrayList<>();
        //Return undefined info if not yet defined
        if(!this.hasValidTrade())
        {
            tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.undefined").withStyle(ChatFormatting.RED));
            return tooltips;
        }

        if(!this.hasStock())
            tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.outofstock").withStyle(ChatFormatting.RED));

        int entryIndex = 1;
        for(SlotMachineEntry entry : this.getValidEntries())
        {
            //Append Info for each entry
            tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.entry", entryIndex++));
            tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.weight_and_odds", entry.getWeight(), this.getOdds(entry.getWeight())));

            if(entry.isMoney())
                tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.money", entry.getMoneyValue().getString("0")));
            else
            {
                //Combine matching items for tooltip purposes (say 128x cobble instead of 64x cobble twice)
                for(ItemStack item : InventoryUtil.combineQueryItems(entry.items))
                    tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.item", item.getCount(), item.getHoverName()));
            }

        }

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
    public final TraderItemStorage getStorage() { return this.storage; }

    public SlotMachineTraderData() { super(TYPE); }
    public SlotMachineTraderData(Level level, BlockPos pos) { super(TYPE, level, pos); }

    private final ImmutableList<SlotMachineTrade> trade = ImmutableList.of(new SlotMachineTrade(this));

    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_TRADER_ALT; }

    @Override
    protected boolean allowAdditionalUpgradeType(UpgradeType type) { return type == UpgradeType.ITEM_CAPACITY; }

    @Override
    public int getTradeCount() { return 1; }

    @Override
    public int getTradeStock(int tradeIndex) { return this.hasStock() ? 1 : 0; }

    public boolean hasStock()
    {
        //Return false if no valid entries exist.
        if(!this.hasValidTrade())
            return false;
        if(this.isCreative())
            return true;
        for(SlotMachineEntry entry : this.entries)
        {
            if(entry.isValid() && !entry.hasStock(this))
                return false;
        }
        return true;
    }

    @Override
    public boolean hasValidTrade() { return this.entries.stream().anyMatch(SlotMachineEntry::isValid) && this.isPriceValid(); }

    @Override
    protected void saveTrades(CompoundTag compound) { }

    @Override
    protected MenuProvider getTraderMenuProvider() {
        return new SlotMachineMenuProvider(this.getID());
        //For now just use the default menu
        //return super.getTraderMenuProvider();
    }

    private record SlotMachineMenuProvider(long traderID) implements MenuProvider {

        @Override
        public AbstractContainerMenu createMenu(int windowID, @Nonnull Inventory inventory, @Nonnull Player player) { return new SlotMachineMenu(windowID, inventory, this.traderID); }

        @Override
        public @Nonnull Component getDisplayName() { return EasyText.empty(); }

    }

    public final void markStorageDirty() { this.markDirty(this::saveStorage); }
    public final void markLastRewardDirty() { this.markDirty(this::saveLastRewards); }
    public final void markEntriesDirty() { this.markDirty(this::saveEntries); }
    public final void markPriceDirty() { this.markDirty(this::savePrice); }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        this.saveStorage(compound);
        this.saveLastRewards(compound);
        this.saveEntries(compound);
        this.savePrice(compound);
    }

    protected final void saveStorage(CompoundTag compound) { this.storage.save(compound,"Storage"); }

    protected final void saveLastRewards(CompoundTag compound) {
        ListTag itemList = new ListTag();
        for(ItemStack reward : this.lastReward)
        {
            if(reward.isEmpty())
                continue;
            itemList.add(reward.save(new CompoundTag()));
        }
        compound.put("LastReward", itemList);
    }

    protected final void saveEntries(CompoundTag compound) {
        ListTag list = new ListTag();
        for(SlotMachineEntry entry : this.entries)
            list.add(entry.save());
        compound.put("Entries", list);
    }

    protected final void savePrice(CompoundTag compound) { this.price.save(compound,"Price"); }

    @Override
    protected void loadAdditional(CompoundTag compound) {

        if(compound.contains("Storage"))
            this.storage.load(compound, "Storage");
        if(compound.contains("LastReward"))
        {
            this.lastReward.clear();
            ListTag itemList = compound.getList("LastReward", Tag.TAG_COMPOUND);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = ItemStack.of(itemList.getCompound(i));
                if(!stack.isEmpty())
                    this.lastReward.add(stack);
            }
        }
        if(compound.contains("Entries"))
        {
            this.entries.clear();
            ListTag list = compound.getList("Entries", Tag.TAG_COMPOUND);
            for(int i = 0; i < list.size(); ++i)
                this.entries.add(SlotMachineEntry.load(list.getCompound(i)));
            this.entriesChanged = true;
        }
        if(compound.contains("Price"))
            this.price.load(compound,"Price");
    }

    //TODO make persistent variant
    @Override
    protected void saveAdditionalToJson(JsonObject json) {
        //Price
        json.add("Price", this.price.toJson());
        //Entries
        JsonArray entryList = new JsonArray();
        for(SlotMachineEntry entry : this.entries)
        {
            if(entry.isValid())
                entryList.add(entry.toJson());
        }
        json.add("Entries", entryList);
    }

    @Override
    protected void loadAdditionalFromJson(JsonObject json) throws Exception {

        if(json.has("Price"))
            this.price = CoinValue.Parse(json.get("Price"));
        else
            throw new RuntimeException("Slot Machine Trader has no 'Price' entry!");

        this.entries.clear();
        if(json.has("Entries"))
        {
            JsonArray entryList = new JsonArray();
            for(int i = 0; i < entryList.size(); ++i)
            {
                try{ this.entries.add(SlotMachineEntry.parse(entryList.get(i).getAsJsonObject()));
                } catch(Throwable t) { LightmansCurrency.LogError("Error parsing Slot Machine Trader Entry #" + (i + 1), t); }
            }
            if(this.entries.size() == 0)
                throw new RuntimeException("Slot Machine Trader had no valid Entries!");
        }
        else
            throw new RuntimeException("Slot Machine Trader has no 'Entries' entry!");

    }

    //No need for persistent data
    @Override
    protected void saveAdditionalPersistentData(CompoundTag compound) {
        this.saveLastRewards(compound);
    }

    @Override
    protected void loadAdditionalPersistentData(CompoundTag compound) {
        if(compound.contains("LastReward"))
        {
            this.lastReward = new ArrayList<>();
            ListTag itemList = compound.getList("LastReward", Tag.TAG_COMPOUND);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = ItemStack.of(itemList.getCompound(i));
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
    public SlotMachineTrade getTrade(int tradeIndex) { return this.trade.get(0); }

    //Trades are not added/removed like other traders
    @Override
    public void addTrade(Player requestor) {}
    @Override
    public void removeTrade(Player requestor) {}

    @Override
    public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {

        if(!this.hasValidTrade())
            return TradeResult.FAIL_INVALID_TRADE;

        SlotMachineTrade trade = this.trade.get(0);
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
        if(this.runPreTradeEvent(context.getPlayerReference(), trade).isCanceled())
            return TradeResult.FAIL_TRADE_RULE_DENIAL;

        //Get the cost of the trade
        CoinValue price = this.runTradeCostEvent(context.getPlayerReference(), trade).getCostResult();

        //Get the Result Items
        SlotMachineEntry loot = this.getRandomizedEntry(context);
        if(loot == null)
        {
            LightmansCurrency.LogError("Slot Machine encountered an error randomizing the loot.");
            return TradeResult.FAIL_NULL;
        }

        //Confirm that the customer can hold the rewards
        //TODO let loot entry confirm this
        if(!loot.CanGiveToCustomer(context))
            return TradeResult.FAIL_NO_OUTPUT_SPACE;

        //Accept the payment
        if(context.getPayment(price))
        {
            //TODO let the loot entry do this
            if(!loot.GiveToCustomer(this, context))
            {
                //Refund the money taken
                context.givePayment(price);
                return TradeResult.FAIL_NO_OUTPUT_SPACE;
            }

            this.lastReward = loot.getDisplayItems();
            this.markLastRewardDirty();

            //Push Notification
            this.pushNotification(() -> new SlotMachineTradeNotification(loot, price, context.getPlayerReference(), this.getNotificationCategory()));

            //Ignore editing internal storage if this is flagged as creative.
            if(!this.isCreative())
            {
                //Give the paid cost to storage
                this.addStoredMoney(price);

                //Push out of stock notification
                if(!this.hasStock())
                    this.pushNotification(() -> new OutOfStockNotification(this.getNotificationCategory(), -1));
            }

            //Push the post-trade event
            this.runPostTradeEvent(context.getPlayerReference(), trade, price);

            return TradeResult.SUCCESS;

        }
        else
            return TradeResult.FAIL_CANNOT_AFFORD;
    }

    @Override
    public void addInteractionSlots(List<InteractionSlotData> interactionSlots) { }

    @Override
    public boolean canMakePersistent() { return true; }

    @Override
    public void initStorageTabs(TraderStorageMenu menu) {

        //Set basic tab to Entry Edit Tab
        menu.setTab(TraderStorageTab.TAB_TRADE_BASIC, new SlotMachineEntryTab(menu));
        //Price tab
        menu.setTab(1, new SlotMachinePriceTab(menu));
        //Storage Tab
        menu.setTab(2, new SlotMachineStorageTab(menu));
    }

    @Override
    protected void addPermissionOptions(List<PermissionOption> options) { }

    @Override
    protected void loadExtraOldUniversalTraderData(CompoundTag compound) { }

    @Override
    protected void loadExtraOldBlockEntityData(CompoundTag compound) { }

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
    public int getStorageStackLimit() {
        int limit = ItemTraderData.DEFAULT_STACK_LIMIT;
        for(int i = 0; i < this.getUpgrades().getContainerSize(); ++i)
        {
            ItemStack stack = this.getUpgrades().getItem(i);
            if(stack.getItem() instanceof UpgradeItem upgradeItem)
            {
                if(this.allowUpgrade(upgradeItem) && upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
                    limit += UpgradeItem.getUpgradeData(stack).getIntValue(CapacityUpgrade.CAPACITY);
            }
        }
        return limit;
    }

}