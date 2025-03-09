package io.github.lightman314.lightmanscurrency.common.traders.gacha;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.GachaMachineBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.items.GachaBallItem;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.gacha_machine.GachaMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.gacha.GachaPriceTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.gacha.GachaStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.GachaTradeNotification;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.tradedata.GachaTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaTrader extends TraderData {

    public static final TraderType<GachaTrader> TYPE = new TraderType<>(VersionUtil.lcResource("gacha"), GachaTrader::new);

    protected GachaTrader() { super(TYPE); }
    public GachaTrader(Level level, BlockPos pos, int color) { super(TYPE,level,pos); this.color = color; }

    private int color = -1;
    public int getColor() {
        if(this.color < 0)
        {
            //Try to get color from block
            if(this.getTraderBlock() instanceof BlockItem bi && bi.getBlock() instanceof GachaMachineBlock block)
                this.color = block.getColor();
            else //Otherwise use a default color
                this.color = 0xFFFFFF;
            this.markDirty(this::saveColor);
        }
        return this.color;
    }

    private MoneyValue price = MoneyValue.empty();
    public MoneyValue getPrice() { return this.price; }
    public void setPrice(@Nullable Player player, MoneyValue price)
    {
        if(this.hasPermission(player, Permissions.EDIT_TRADES))
        {
            this.price = price;
            this.markDirty(this::savePrice);
        }
    }

    private final GachaStorage storage = new GachaStorage(this::getMaxItems);
    public GachaStorage getStorage() { return this.storage; }

    private final List<GachaTradeData> trades = ImmutableList.of(new GachaTradeData(this));

    public void markStorageDirty() { this.markDirty(this::saveStorage); }

    @Override
    public IconData getIcon() { return IconData.of(GachaBallItem.createWithItemAndColor(new ItemStack(ModItems.TRADING_CORE.get()),Color.YELLOW)); }

    @Override
    protected boolean allowAdditionalUpgradeType(UpgradeType type) { return type == Upgrades.ITEM_CAPACITY; }

    public int getMaxItems() {
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

    @Override
    public int getTradeCount() { return 1; }

    @Override
    public int getTradeStock(int tradeIndex) { return this.storage.getItemCount(); }

    @Override
    protected void saveTrades(CompoundTag compound, HolderLookup.Provider lookup) { }

    @Override
    protected MenuProvider getTraderMenuProvider(@Nonnull MenuValidator validator) { return new GachaMachineMenuProvider(this.getID(),validator); }

    private record GachaMachineMenuProvider(long traderID, MenuValidator validator) implements EasyMenuProvider {
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) { return new GachaMachineMenu(containerId,playerInventory,this.traderID,this.validator); }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
         this.saveStorage(compound,lookup);
         this.savePrice(compound,lookup);
         this.saveColor(compound);
    }

    protected void saveStorage(CompoundTag tag, HolderLookup.Provider lookup) {
        tag.put("Storage",this.storage.save(lookup));
    }

    protected void savePrice(CompoundTag tag, HolderLookup.Provider lookup) {
        tag.put("Price",this.price.save());
    }

    protected void saveColor(CompoundTag tag) { tag.putInt("Color",this.color); }

    @Override
    protected void saveAdditionalToJson(JsonObject json, HolderLookup.Provider lookup) {
        json.add("Items",this.storage.write(lookup));
        json.add("Price",this.price.toJson());
        json.addProperty("Color",this.getColor());
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        if(compound.contains("Storage"))
            this.storage.load(compound.getList("Storage",Tag.TAG_COMPOUND),lookup);
        if(compound.contains("Price"))
            this.price = MoneyValue.load(compound.getCompound("Price"));
        if(compound.contains("Color"))
            this.color = compound.getInt("Color");
    }

    @Override
    protected void loadAdditionalFromJson(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
        this.price = MoneyValue.loadFromJson(GsonHelper.getAsJsonObject(json,"Price"));
        this.storage.read(GsonHelper.getAsJsonArray(json,"Storage"),lookup);
        this.color = GsonHelper.getAsInt(json,"Color",0xFFFFFF);
    }

    @Override
    protected void saveAdditionalPersistentData(CompoundTag compound, HolderLookup.Provider lookup) { }

    @Override
    protected void loadAdditionalPersistentData(CompoundTag compound, HolderLookup.Provider lookup) { }

    @Override
    protected void getAdditionalContents(List<ItemStack> results) {
        results.addAll(this.storage.getSplitContents());
    }

    @Nonnull
    @Override
    public List<GachaTradeData> getTradeData() { return this.trades; }

    @Nullable
    @Override
    public TradeData getTrade(int tradeIndex) { return this.trades.getFirst(); }

    @Override
    public void addTrade(Player requestor) { }
    @Override
    public void removeTrade(Player requestor) { }

    @Override
    protected TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {

        if(tradeIndex != 0 || !this.price.isValidPrice())
            return TradeResult.FAIL_INVALID_TRADE;

        if(this.storage.isEmpty())
            return TradeResult.FAIL_OUT_OF_STOCK;

        MoneyValue cost = this.trades.getFirst().getCost(context);
        if(!context.hasFunds(cost))
            return TradeResult.FAIL_CANNOT_AFFORD;

        //Check if they can hold the item
        ItemStack result = this.storage.removeRandomItem();
        ItemStack gatchaBall = GachaBallItem.createWithItem(result);
        if(!context.canFitItem(gatchaBall))
        {
            this.storage.forceInsertItem(result);
            return TradeResult.FAIL_NO_OUTPUT_SPACE;
        }

        //Actually take the money
        if(!context.getPayment(cost))
            return TradeResult.FAIL_CANNOT_AFFORD;

        //Give the player the item
        if(!context.putItem(gatchaBall))
        {
            context.givePayment(cost);
            this.storage.forceInsertItem(result);
            return TradeResult.FAIL_NO_OUTPUT_SPACE;
        }

        MoneyValue taxesPaid = MoneyValue.empty();
        if(this.canStoreMoney())
            taxesPaid = this.addStoredMoney(cost,true);
        //Put the item back in storage if we're a creative trader
        if(this.isCreative())
            this.storage.insertItem(result);
        else
            this.markStorageDirty();

        //Push Notification
        this.pushNotification(GachaTradeNotification.create(result,cost,context.getPlayerReference(),this.getNotificationCategory(),taxesPaid));

        return TradeResult.SUCCESS;
    }

    @Override
    public boolean canMakePersistent() { return true; }

    @Override
    public void initStorageTabs(ITraderStorageMenu menu) {
        menu.setTab(TraderStorageTab.TAB_TRADE_BASIC,new GachaStorageTab(menu));
        menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE,new GachaPriceTab(menu));
    }

    @Override
    protected void addPermissionOptions(List<PermissionOption> options) { }

}
