package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsStorageIONode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStorageMenuTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.PlayerSyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.persistent.PersistentDataException;
import io.github.lightman314.lightmanscurrency.api.trader.rules.*;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.GlobalTradeRuleTab;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class TradeRulesNode extends PlayerSyncedTraderNode implements TradeRuleHolder, IStorageMenuTabProvider, ISettingsStorageIONode, IPersistentNode {

    private static final MapCodec<TradeRulesNode> MAP_CODEC = TradeRule.SET_CODEC.fieldOf("rules")
            .xmap(TradeRulesNode::new,TradeRulesNode::getEncodingRuleData);

    public static final TraderNodeType<TradeRulesNode> TYPE = TraderNodeType.simple(TradeRulesNode::new,MAP_CODEC);

    public static final TextEntry NAME = TextEntry.traderNode(TYPE);
    public static final TextEntry VALUE_RULES = TextEntry.traderNodeValue(TYPE,"trade_rules");

    private final Map<TradeRuleType<?>,TradeRule> rules = new HashMap<>();
    @Override
    public Map<TradeRuleType<?>, TradeRule> getRuleData() { return Collections.unmodifiableMap(this.rules); }

    @Override
    public void removeUnusedRule(TradeRule rule) { this.rules.remove(rule.getType()); }

    private TradeRulesNode() {}
    private TradeRulesNode(Map<TradeRuleType<?>,TradeRule> rules) { this.rules.putAll(rules); }

    @Override
    public void setRuleChanged(TradeRuleType<?> type, Consumer<FancyPacketMap.Mutable> writer, Predicate<ISyncingContext> filter) {
        this.setChanged(data -> data.modifyMap(type.getKey().toString(),writer),filter);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onAttach() {
        this.validateRuleStates(this.rules);
        //Attach the Rules
        TradeRule.attach(this.rules,this);
    }

    @Override
    public void validateRuleStates() { this.validateRuleStates(this.rules); }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        this.rules.forEach((type,rule) -> {
            FancyPacketMap.Mutable entry = FancyPacketMap.map();
            rule.createFullSyncPacket(entry,context);
            if(!entry.isEmpty())
                builder.setMap(type.getKey().toString(),entry);
        });
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        for(String key : data.keySet()) {
            try {
                Identifier typeID = Identifier.parse(key);
                TradeRuleType<?> type = LCRegistries.Trader.TRADE_RULE_TYPE.getValue(typeID);
                if(type != null && this.rules.containsKey(type))
                    this.rules.get(type).handleSyncPacket(data.getMap(key));
            }catch (IdentifierException ignored) {}
        }
    }

    @Override
    public void addTabs(StorageTabBuilder builder) { builder.addTab(GlobalTradeRuleTab::new); }

    private TradeRuleSettingsWrapper buildWrapper() { return new TradeRuleSettingsWrapper(this,this.getSettingsKey(),NAME.get(),c -> c.getPermission(LCPermissions.EDIT_TRADE_RULES)); }

    @Override
    public void encodeSettings(ValueOutput output) {
        this.buildWrapper().encodeSettings(output);
    }

    @Override
    public void decodeSettings(ValueInput data,SettingsLoadContext context) {
        this.buildWrapper().decodeSettings(data,context);
    }

    @Override
    public void appendDisplay(ValueInput data,SettingsDisplayOutput output) {
        this.buildWrapper().appendDisplay(data,output);
    }

    @Override
    public void writePersistentData(ValueOutput output,String nameOverride) throws PersistentDataException {
        TradeRuleHolder.writePersistentRuleData(this,output);
    }

    @Override
    public void readPersistentData(ValueInput input,ProblemReporter reporter) {
        TradeRuleHolder.readPersistentRuleData(this,input,reporter);
    }

    @Override
    public void writePersistentSaveData(ValueOutput output) {
        TradeRuleHolder.writePersistentRuleSaveData(this,output);
    }

    @Override
    public void readPersisentSaveData(ValueInput input) {
        TradeRuleHolder.readPersistentRuleSaveData(this,input);
    }
}