package io.github.lightman314.lightmanscurrency.common.traders.gacha;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.GachaItemHandler;
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
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.tradedata.GachaTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaTrader extends InputTraderData {

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

    private final GachaItemHandler handler = new GachaItemHandler(this);
    private final GachaStorage storage = new GachaStorage(this::getMaxItems);
    public GachaStorage getStorage() { return this.storage; }
    public IItemHandler getStorageWrapper() { return this.handler.getFullyAuthorizedHandler(); }

    public IItemHandler getItemHandler(Direction side) { return this.handler.getHandler(side); }

    private final List<GachaTradeData> trades = ImmutableList.of(new GachaTradeData(this));

    public void markStorageDirty() { this.markDirty(this::saveStorage); }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(GachaBallItem.createWithItemAndColor(new ItemStack(ModItems.TRADING_CORE.get()),Color.YELLOW)); }

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
    protected void saveTrades(CompoundTag compound) { }

    @Override
    protected MenuProvider getTraderMenuProvider(MenuValidator validator) { return new GachaMachineMenuProvider(this.getID(),validator); }

    private record GachaMachineMenuProvider(long traderID, MenuValidator validator) implements EasyMenuProvider {
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) { return new GachaMachineMenu(containerId,playerInventory,this.traderID,this.validator); }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        this.saveStorage(compound);
        this.savePrice(compound);
        this.saveColor(compound);
    }

    protected void saveStorage(CompoundTag tag) {
        tag.put("Storage",this.storage.save());
    }

    protected void savePrice(CompoundTag tag) {
        tag.put("Price",this.price.save());
    }

    protected void saveColor(CompoundTag tag) { tag.putInt("Color",this.color); }

    @Override
    protected void saveAdditionalToJson(JsonObject json) {
        json.add("Items",this.storage.write());
        json.add("Price",this.price.toJson());
        json.addProperty("Color",this.getColor());
    }

    @Override
    protected void loadAdditional(CompoundTag compound) {
        super.loadAdditional(compound);
        if(compound.contains("Storage"))
            this.storage.load(compound.getList("Storage",Tag.TAG_COMPOUND));
        if(compound.contains("Price"))
            this.price = MoneyValue.load(compound.getCompound("Price"));
        if(compound.contains("Color"))
            this.color = compound.getInt("Color");
    }

    @Override
    public IconData inputSettingsTabIcon() { return ItemIcon.ofItem(Items.HOPPER); }

    @Override
    public MutableComponent inputSettingsTabTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_INPUT_ITEM.get(); }

    @Override
    protected void loadAdditionalFromJson(JsonObject json) throws JsonSyntaxException, ResourceLocationException {
        this.price = MoneyValue.loadFromJson(GsonHelper.getAsJsonObject(json,"Price"));
        this.storage.read(GsonHelper.getAsJsonArray(json,"Storage"));
        this.color = GsonHelper.getAsInt(json,"Color",0xFFFFFF);
    }

    @Override
    protected void saveAdditionalPersistentData(CompoundTag compound) { }

    @Override
    protected void loadAdditionalPersistentData(CompoundTag compound) { }

    @Override
    protected void getAdditionalContents(List<ItemStack> results) {
        results.addAll(this.storage.getSplitContents());
    }

    @Override
    public List<GachaTradeData> getTradeData() { return this.trades; }

    @Nullable
    @Override
    public TradeData getTrade(int tradeIndex) { return this.trades.get(0); }

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

        GachaTradeData trade = this.trades.get(0);

        //Check if the player is allowed to do the trade
        if(this.runPreTradeEvent(trade,context).isCanceled())
            return TradeResult.FAIL_TRADE_RULE_DENIAL;

        MoneyValue cost = trade.getCost(context);
        if(!context.hasFunds(cost))
            return TradeResult.FAIL_CANNOT_AFFORD;

        //Check if they can hold the item
        ItemStack result = this.storage.findRandomItem(!this.isCreative());
        ItemStack gachaBall = GachaBallItem.createWithItem(result);
        if(!context.canFitItem(gachaBall))
        {
            //Put the item back into storage (unless we're creative as we didn't actually remove it then)
            if(!this.isCreative())
            {
                this.storage.forceInsertItem(result);
                this.markStorageDirty();
            }
            return TradeResult.FAIL_NO_OUTPUT_SPACE;
        }

        //Actually take the money
        if(!context.getPayment(cost))
            return TradeResult.FAIL_CANNOT_AFFORD;

        //Give the player the item
        if(!context.putItem(gachaBall))
        {
            //Failed to give the customer the item, so give a refund and put the reward back into storage
            context.givePayment(cost);
            if(!this.isCreative())
            {
                this.storage.forceInsertItem(result);
                this.markStorageDirty();
            }
            return TradeResult.FAIL_NO_OUTPUT_SPACE;
        }

        MoneyValue taxesPaid = MoneyValue.empty();
        if(this.canStoreMoney())
            taxesPaid = this.addStoredMoney(cost,context.getTaxContext());
        //Flag the trader storage as changed
        if(!this.isCreative())
            this.markStorageDirty();

        //Push Notification
        this.pushNotification(GachaTradeNotification.create(result,cost,context.getPlayerReference(),this.getNotificationCategory(),taxesPaid));

        //Run the post-trade event
        this.runPostTradeEvent(trade,context,price,taxesPaid,gachaBall);

        return TradeResult.SUCCESS;
    }

    @Override
    public boolean canMakePersistent() { return true; }

    @Override
    public void initStorageTabs(ITraderStorageMenu menu) {
        menu.setTab(TraderStorageTab.TAB_TRADE_BASIC,new GachaStorageTab(menu));
        menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE,new GachaPriceTab(menu));
    }

}
