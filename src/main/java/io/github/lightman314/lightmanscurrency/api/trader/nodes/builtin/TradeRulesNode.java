package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStorageMenuTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.PlayerSyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.GlobalTradeRuleTab;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class TradeRulesNode extends PlayerSyncedTraderNode implements TradeRuleHolder, IStorageMenuTabProvider {

    private static final MapCodec<TradeRulesNode> MAP_CODEC = TradeRule.SET_CODEC.fieldOf("rules")
            .xmap(TradeRulesNode::new,TradeRulesNode::getEncodingRuleData);

    public static final TraderNodeType<TradeRulesNode> TYPE = TraderNodeType.simple(TradeRulesNode::new,MAP_CODEC);

    private final Map<TradeRuleType<?>,TradeRule> rules = new HashMap<>();
    @Override
    public Map<TradeRuleType<?>, TradeRule> getRuleData() { return Collections.unmodifiableMap(this.rules); }

    private TradeRulesNode() {}
    private TradeRulesNode(Map<TradeRuleType<?>,TradeRule> rules) {
        this.rules.putAll(rules);
    }

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
    public void addTabs(StorageTabBuilder builder) {
        builder.addTab(GlobalTradeRuleTab::new);
    }

}
