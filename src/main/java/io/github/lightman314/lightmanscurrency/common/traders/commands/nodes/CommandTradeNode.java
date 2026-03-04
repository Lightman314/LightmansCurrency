package io.github.lightman314.lightmanscurrency.common.traders.commands.nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;

import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ITerminalDisplay;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tabs.CommandTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.common.traders.commands.trade.CommandTrade;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CommandTradeNode extends TradeOfferSourceNode<CommandTrade> implements ITerminalDisplay, IPersistentNode {

    private static final MapCodec<CommandTradeNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("permission_level").forGetter(CommandTradeNode::getPermissionLevel),
            CommandTrade.CODEC.listOf(1,TraderData.GLOBAL_TRADE_LIMIT).fieldOf("trades").forGetter(CommandTradeNode::getAllTrades)
    ).apply(builder,CommandTradeNode::new));
    public static final TraderNodeType<CommandTradeNode> TYPE = TraderNodeType.simple(CommandTradeNode::new,MAP_CODEC);

    private int permissionLevel = 2;
    public int getPermissionLevel() { return MathUtil.clamp(this.permissionLevel,0, LCConfig.SERVER.commandTraderMaxPermissionLevel.get()); }
    public void setPermissionLevel(@Nullable PlayerReference admin, int newValue) {
        newValue = MathUtil.clamp(newValue,0,LCConfig.SERVER.commandTraderMaxPermissionLevel.get());
        if(this.permissionLevel != newValue)
        {
            this.permissionLevel = newValue;
            this.setChanged(builder -> builder.setInt("permissionLevel",this.permissionLevel));
            if(admin != null) //TODO add settings node and proper translation for this data entry
                this.pushLocalNotification(ChangeSettingNotification.simple(admin,EasyText.literal("(WIP) Command Permission Level"),this.permissionLevel));
        }
    }
    private final List<CommandTrade> trades;

    private CommandTradeNode() {
        this.trades = CommandTrade.listOfSize(1,true);
        TradeData.afterLoad(this.trades,this);
    }
    private CommandTradeNode(int permissionLevel,List<CommandTrade> trades)
    {
        this.permissionLevel = permissionLevel;
        this.trades = new ArrayList<>(trades);
        TradeData.afterLoad(this.trades,this);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public int getMaxTradeCount() { return TraderData.GLOBAL_TRADE_LIMIT; }

    @Override
    public boolean addTrade(Player player) {
        if(this.hasPermission(player,Permissions.EDIT_TRADES))
        {
            if(this.getTradeCount() >= TraderData.GLOBAL_TRADE_LIMIT)
                return false;
            CommandTrade newTrade = new CommandTrade(true);
            this.trades.add(newTrade);
            TradeData.afterLoad(newTrade,this);
            this.setTradeChanged(this.trades.size() - 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeTrade(Player player) {
        if(this.hasPermission(player,Permissions.EDIT_TRADES))
        {
            if(this.trades.size() <= 1)
                return false;
            this.trades.removeLast();
            this.setTradeChanged(this.trades.size());
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsTradeRules() { return true; }
    @Override
    public boolean canEasilyChangeQuantity() { return true; }
    @Override
    protected List<CommandTrade> getEditableList() { return this.trades; }
    @Override
    protected Supplier<LazyPacketType<CommandTrade>> getPacketType() { return ModLazyPackets.COMMAND_TRADE; }
    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("PermissionLevel"))
            this.permissionLevel = tag.getInt("PermissionLevel");
        if(tag.contains("Trades"))
        {
            this.trades.clear();
            ListTag list = tag.getList("Trades", Tag.TAG_COMPOUND);
            List<CommandTrade> trades = new ArrayList<>();
            DataContext<Tag> context = DataContext.createNBT(lookup);
            for(int i = 0; i < list.size(); ++i)
                this.trades.add(CommandTrade.CODEC.decode(context.ops(),list.getCompound(i)).getOrThrow().getFirst());
            TradeData.afterLoad(this.trades,this);
        }
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        super.handleSettingsChange(player, message);
        if(message.contains("ChangePermissionLevel") && this.hasPermission(player, Permissions.EDIT_SETTINGS))
            this.setPermissionLevel(PlayerReference.of(player),message.getInt("ChangePermissionLevel"));
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        super.applyStorageTabs(menu);
        menu.addTab(new CommandTradeEditTab(menu));
    }

    @Override
    public void addTerminalInfo(List<Component> tooltip, @Nullable Player player) {
        tooltip.add(LCText.TOOLTIP_NETWORK_TERMINAL_TRADE_COUNT.get(this.trades.stream().filter(CommandTrade::isValid).count()));
    }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {
        json.addProperty("PermissionLevel",this.permissionLevel);

        JsonArray trades = new JsonArray();
        for(CommandTrade trade : this.trades)
        {
            if(trade.isValid())
            {
                JsonObject tradeData = new JsonObject();
                tradeData.add("Price",trade.getCost().toJson());
                tradeData.addProperty("Command",trade.getCommand());
                if(!trade.getDescription().isBlank())
                    tradeData.addProperty("Description",trade.getDescription());
                if(!trade.getTooltip().isBlank())
                    tradeData.addProperty("Tooltip",trade.getTooltip());

                JsonObject ruleData = TradeRule.savePersistentRules(trade.getRuleMap(),context);
                if(!ruleData.isEmpty())
                    tradeData.add("Rules", ruleData);

                trades.add(tradeData);
            }
        }
        json.add("Trades",trades);
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        JsonArray trades = GsonHelper.getAsJsonArray(json, "Trades");

        this.trades.clear();
        for(int i = 0; i < trades.size() && this.trades.size() < TraderData.GLOBAL_TRADE_LIMIT; ++i)
        {
            try {
                JsonObject tradeData = GsonHelper.convertToJsonObject(trades.get(i),"Trades[" + i + "]");

                CommandTrade newTrade = new CommandTrade(false);

                //Trade Price
                newTrade.setCost(MoneyValue.loadFromJson(tradeData.get("Price")));

                //Command
                newTrade.setCommand(GsonHelper.getAsString(tradeData,"Command"));
                newTrade.setDescription(GsonHelper.getAsString(tradeData,"Description",""));
                newTrade.setTooltip(GsonHelper.getAsString(tradeData,"Tooltip",""));

                if(tradeData.has("Rules"))
                    newTrade.setRules(TradeRule.loadPersistentRules(tradeData, "Rules", context));

                this.trades.add(newTrade);

            } catch(Exception e) { LightmansCurrency.LogError("Error parsing command trade at index " + i, e); }
        }

        this.permissionLevel = GsonHelper.getAsInt(json,"PermissionLevel",2);

        if(this.trades.isEmpty())
            throw new JsonSyntaxException("Trader has no valid trades!");

    }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) { return this.writePersistentRuleTag(context); }
    @Override
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) { this.readPersistentRuleTag(tag,context); }

}
