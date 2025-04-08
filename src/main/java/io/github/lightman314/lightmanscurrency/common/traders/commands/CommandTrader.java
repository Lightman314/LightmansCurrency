package io.github.lightman314.lightmanscurrency.common.traders.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.command.CommandSettingsTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.misc.UpgradesTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.command.CommandTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.CommandTradeNotification;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.CommandTrade;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandTrader extends TraderData {

    public static final TraderType<CommandTrader> TYPE = new TraderType<>(VersionUtil.lcResource("commands"),CommandTrader::new);

    private int permissionLevel = 2;
    public int getPermissionLevel() { return MathUtil.clamp(this.permissionLevel,0,LCConfig.SERVER.commandTraderMaxPermissionLevel.get()); }
    public void setPermissionLevel(@Nullable Player player, int newValue) {
        if(this.hasPermission(player,Permissions.EDIT_SETTINGS))
        {
            this.permissionLevel = MathUtil.clamp(newValue,0, LCConfig.SERVER.commandTraderMaxPermissionLevel.get());
            this.markDirty(this::savePermissionLevel);
        }
    }
    private List<CommandTrade> trades = new ArrayList<>();

    private CommandTrader() {super(TYPE); }
    public CommandTrader(Level level, BlockPos pos) {
        super(TYPE,level,pos);
        this.trades = CommandTrade.listOfSize(1,true);
    }

    @Override
    public IconData getIcon() { return IconData.of(Items.COMMAND_BLOCK); }

    @Override
    protected boolean allowAdditionalUpgradeType(UpgradeType type) { return false; }

    @Override
    public boolean canEditTradeCount() { return true; }

    @Override
    public int getMaxTradeCount() { return TraderData.GLOBAL_TRADE_LIMIT; }

    @Override
    public int getTradeCount() { return this.trades.size(); }

    @Override
    public int getTradeStock(int tradeIndex) {
        CommandTrade trade = this.getTrade(tradeIndex);
        if(trade == null || !trade.isValid())
            return 0;
        return 1;
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        this.savePermissionLevel(compound);
        this.saveTrades(compound);
    }

    protected void savePermissionLevel(CompoundTag compound) {
        compound.putInt("PermissionLevel",this.permissionLevel);
    }

    @Override
    protected void saveTrades(CompoundTag compound) {
        ListTag list = new ListTag();
        for(CommandTrade trade : new ArrayList<>(this.trades))
            list.add(trade.getAsNBT());
        compound.put("Trades",list);
    }

    @Override
    protected void saveAdditionalToJson(JsonObject json) {

        json.addProperty("PermissionLevel",this.permissionLevel);

        JsonArray trades = new JsonArray();
        for(CommandTrade trade : this.trades)
        {
            if(trade.isValid())
            {
                JsonObject tradeData = new JsonObject();
                tradeData.add("Price",trade.getCost().toJson());
                tradeData.addProperty("Command",trade.getCommand());

                JsonArray ruleData = TradeRule.saveRulesToJson(trade.getRules());
                if(!ruleData.isEmpty())
                    tradeData.add("Rules", ruleData);

                trades.add(tradeData);
            }
        }
        json.add("Trades",trades);
    }

    @Override
    protected void loadAdditional(CompoundTag compound) {
        if(compound.contains("PermissionLevel"))
            this.permissionLevel = compound.getInt("PermissionLevel");
        if(compound.contains("Trades"))
        {
            this.trades = new ArrayList<>();
            ListTag list = compound.getList("Trades",Tag.TAG_COMPOUND);
            List<CommandTrade> trades = new ArrayList<>();
            for(int i = 0; i < list.size(); ++i)
                this.trades.add(CommandTrade.loadData(list.getCompound(i),!this.isPersistent()));
        }
    }

    @Override
    protected void loadAdditionalFromJson(JsonObject json) throws JsonSyntaxException, ResourceLocationException {

        JsonArray trades = GsonHelper.getAsJsonArray(json, "Trades");

        this.trades = new ArrayList<>();
        for(int i = 0; i < trades.size() && this.trades.size() < TraderData.GLOBAL_TRADE_LIMIT; ++i)
        {
            try {
                JsonObject tradeData = GsonHelper.convertToJsonObject(trades.get(i),"Trades[" + i + "]");

                CommandTrade newTrade = new CommandTrade(false);

                //Trade Price
                newTrade.setCost(MoneyValue.loadFromJson(tradeData.get("Price")));

                //Command
                newTrade.setCommand(GsonHelper.getAsString(tradeData,"Command"));

                if(tradeData.has("Rules"))
                    newTrade.setRules(TradeRule.Parse(GsonHelper.getAsJsonArray(tradeData, "Rules"), newTrade));

                this.trades.add(newTrade);

            } catch(Exception e) { LightmansCurrency.LogError("Error parsing command trade at index " + i, e); }
        }

        this.permissionLevel = GsonHelper.getAsInt(json,"PermissionLevel",2);

        if(this.trades.isEmpty())
            throw new JsonSyntaxException("Trader has no valid trades!");

    }

    @Override
    protected void saveAdditionalPersistentData(CompoundTag compound) {
        ListTag tradePersistentData = new ListTag();
        boolean tradesAreRelevant = false;
        for (CommandTrade trade : this.trades) {
            CompoundTag ptTag = new CompoundTag();
            if (TradeRule.savePersistentData(ptTag, trade.getRules(), "RuleData"))
                tradesAreRelevant = true;
            tradePersistentData.add(ptTag);
        }
        if(tradesAreRelevant)
            compound.put("PersistentTradeData", tradePersistentData);
    }

    @Override
    protected void loadAdditionalPersistentData(CompoundTag compound) {
        if(compound.contains("PersistentTradeData"))
        {
            ListTag tradePersistentData = compound.getList("PersistentTradeData", Tag.TAG_COMPOUND);
            for(int i = 0; i < tradePersistentData.size() && i < this.trades.size(); ++i)
            {
                CommandTrade trade = this.trades.get(i);
                CompoundTag ptTag = tradePersistentData.getCompound(i);
                TradeRule.loadPersistentData(ptTag, trade.getRules(), "RuleData");
            }
        }
    }

    @Override
    protected void getAdditionalContents(List<ItemStack> results) { }

    @Override
    public List<? extends TradeData> getTradeData() { return new ArrayList<>(this.trades); }

    @Nullable
    @Override
    public CommandTrade getTrade(int tradeIndex) {
        if(tradeIndex < 0 || tradeIndex >= this.trades.size())
            return null;
        return this.trades.get(tradeIndex);
    }

    @Override
    public void addTrade(Player requestor) {
        if(this.hasPermission(requestor, Permissions.EDIT_TRADES))
        {
            if(this.getTradeCount() >= GLOBAL_TRADE_LIMIT)
                return;
            this.trades.add(new CommandTrade(true));
            this.markTradesDirty();
        }
    }

    @Override
    public void removeTrade(Player requestor) {
        if(this.hasPermission(requestor,Permissions.EDIT_TRADES))
        {
            if(this.trades.size() <= 1)
                return;
            this.trades.remove(this.trades.size() - 1);
            this.markTradesDirty();
        }
    }

    @Override
    protected TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
        CommandTrade trade = this.getTrade(tradeIndex);
        if(trade == null || !trade.isValid())
            return TradeResult.FAIL_INVALID_TRADE;
        if(context.getPlayer() instanceof ServerPlayer player)
        {

            if(this.runPreTradeEvent(trade,context).isCanceled())
                return TradeResult.FAIL_TRADE_RULE_DENIAL;

            MoneyValue price = trade.getCost(context);
            if(!context.getPayment(price))
                return TradeResult.FAIL_CANNOT_AFFORD;

            //Give the paid cost to storage
            MoneyValue taxesPaid = MoneyValue.empty();
            if(this.canStoreMoney())
                taxesPaid = this.addStoredMoney(price, true);

            //Run Command
            player.server.getCommands().performPrefixedCommand(this.sourceForPlayer(player),trade.formatCommand(player));

            //Handle Stats
            this.incrementStat(StatKeys.Traders.MONEY_EARNED, price);
            if(!taxesPaid.isEmpty())
                this.incrementStat(StatKeys.Taxables.TAXES_PAID,taxesPaid);

            //Push Notification
            this.pushNotification(CommandTradeNotification.create(trade,price,context.getPlayerReference(),this.getNotificationCategory(),taxesPaid));

            //Push the post-trade event
            this.runPostTradeEvent(trade, context, price, taxesPaid);

            return TradeResult.SUCCESS;

        }
        else
            return TradeResult.FAIL_NOT_SUPPORTED;
    }

    private CommandSourceStack sourceForPlayer(ServerPlayer player) {
        return new CommandSourceStack(player,player.position(),player.getRotationVector(),player.serverLevel(),this.getPermissionLevel(),player.getName().getString(),player.getName(),player.server,player);
    }

    @Override
    public boolean canMakePersistent() { return true; }

    @Override
    public void initStorageTabs(ITraderStorageMenu menu) {
        menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE,new UpgradesTab(menu,1));
        menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED,new CommandTradeEditTab(menu));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void addPermissionOptions(List<PermissionOption> options) { }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addSettingsTabs(TraderSettingsClientTab tab, List<SettingsSubTab> tabs) {
        tabs.add(new CommandSettingsTab(tab));
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        super.handleSettingsChange(player, message);
        if(message.contains("ChangePermissionLevel"))
            this.setPermissionLevel(player,message.getInt("ChangePermissionLevel"));
    }

    @Override
    protected void appendTerminalInfo(List<Component> list, @Nullable Player player) {
        list.add(LCText.TOOLTIP_NETWORK_TERMINAL_TRADE_COUNT.get(this.validTradeCount()));
    }

}