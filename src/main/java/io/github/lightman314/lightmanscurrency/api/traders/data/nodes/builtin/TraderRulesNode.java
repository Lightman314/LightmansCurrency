package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.google.gson.*;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.PlayerSyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.TradeRulesTab;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.TraderRuleSettings;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ITradeListener;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TraderRulesNode extends PlayerSyncedTraderNode implements ITradeListener, IPersistentNode {

    private static final MapCodec<TraderRulesNode> MAP_CODEC = TradeRule.SET_CODEC.fieldOf("rules").xmap(TraderRulesNode::new,n -> n.rules);

    public static final TraderNodeType<TraderRulesNode> TYPE = TraderNodeType.simple(TraderRulesNode::new,MAP_CODEC);

    private boolean validate = true;
    private final Map<TradeRuleType<?>,TradeRule> rules;
    public Map<TradeRuleType<?>,TradeRule> getRules() { return this.rules; }
    @Nullable
    public TradeRule getRule(TradeRuleType<?> type) { return this.rules.get(type); }
    public TradeRule addRule(TradeRuleType<?> type)
    {
        if(this.isServer())
            return null;
        TradeRule rule = type.create();
        if(rule.allowHost(this.trader) && this.trader.allowTradeRule(rule))
        {
            this.rules.put(type,rule);
            TradeRule.AfterRulesLoaded(this.rules,this.trader,false);
            return rule;
        }
        return null;
    }

    private TraderRulesNode() { this(new HashMap<>()); }
    private TraderRulesNode(Map<TradeRuleType<?>,TradeRule> rules) { this.rules = new HashMap<>(rules); }

    public void setRuleChanged(TradeRuleType<?> type) {
        if(this.rules.containsKey(type))
            this.setChanged((player,builder) -> builder.modifyMap("rules",m -> TradeRule.encodeRule(m,this::builder,this.trader,type,player)));
    }

    @Override
    public void onRegisteredToOffice() {
        if(TradeRule.AfterRulesLoaded(this.rules,this.trader,this.isServer() && this.validate))
            this.setChanged(builder -> {}); //Don't need to write to packet, as the sync packets should not have been sent yet
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        builder.setMap("rules",TradeRule.encodeRules(this::builder,this.trader,player));
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("rules"))
            TradeRule.decodeRules(data.getMap("rules"),this.trader);
    }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {
        for(TradeRule rule : this.rules.values())
            rule.beforeTrade(event);
    }

    @Override
    public void tradeCost(TradeEvent.TradeCostEvent event) {
        for(TradeRule rule : this.rules.values())
            rule.tradeCost(event);
    }

    @Override
    public void afterTrade(TradeEvent.PostTradeEvent event) {
        for(TradeRule rule : this.rules.values())
        {
            if(rule.afterTrade(event))
                this.setRuleChanged(rule.getType());
        }
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        super.handleSettingsChange(player, message);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("RuleData"))
        {
            this.rules.clear();
            this.rules.putAll(TradeRule.loadOldRules(tag, "RuleData", this.trader, DataContext.createNBT(lookup)));
        }
    }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {
        JsonObject entry = TradeRule.savePersistentRules(this.rules,context);
        if(!entry.isEmpty())
            json.add("Rules",entry);
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        this.rules.clear();
        this.validate = false;
        if(json.has("Rules"))
        {
            this.rules.putAll(TradeRule.loadPersistentRules(json,"Rules",context));
            this.validate = false;
        }
    }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) {
        ListTag list = TradeRule.savePersistentData(this.rules,context);
        if(!list.isEmpty())
        {
            CompoundTag tag = new CompoundTag();
            tag.put("RuleData",list);
            return tag;
        }
        return null;
    }

    @Override
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) {
        if(tag.contains("RuleData"))
            TradeRule.loadPersistentData(tag.getList("RuleData",Tag.TAG_COMPOUND),this.rules,context);
    }

    @Override
    public void registerSettingsNodes(TraderData trader, Consumer<SettingsNode> consumer) {
        consumer.accept(new TraderRuleSettings(trader,this));
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.EDIT_TRADE_RULES, 1);
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        menu.addTab(new TradeRulesTab.Trader(menu));
    }

}
