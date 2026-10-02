package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class TradeDataWithRules extends TradeData implements TradeRuleHolder {

    private final Map<TradeRuleType<?>,TradeRule> rules = new HashMap<>();

    @Override
    public Map<TradeRuleType<?>, TradeRule> getRuleData() { return Collections.unmodifiableMap(this.rules); }

    public static <T extends TradeDataWithRules> RecordCodecBuilder<T,Map<TradeRuleType<?>,TradeRule>> tradeRuleArg() {
        return TradeRule.SET_CODEC.optionalFieldOf("rules",Map.of()).forGetter(TradeDataWithRules::getEncodingRuleData);
    }

    private boolean validateRules = true;
    //Allow overriding so that it can return the more specific child class
    public final void dontValidateRules() { this.validateRules = false; }

    @Override
    protected void afterInit() {
        if(this.validateRules)
            this.validateRuleStates(this.rules);
        //Attach the Rules
        TradeRule.attach(this.rules,this);
    }

    public TradeDataWithRules() { super(); }
    public TradeDataWithRules(TradePrice price,Map<TradeRuleType<?>,TradeRule> rules) {
        super(price);
        this.rules.putAll(rules);
    }

    @Override
    public void setRuleChanged(TradeRuleType<?> type, Consumer<FancyPacketMap.Mutable> writer, Predicate<ISyncingContext> filter) {
        this.setChanged(data ->
            data.modifyMap("rule_update",ruleMap ->
                ruleMap.modifyMap(type.getKey().toString(),writer)),filter);
    }

    @Override
    public void getFullPacket(FancyPacketMap.Mutable packet, ISyncingContext context) {
        super.getFullPacket(packet, context);
        this.getFullRulePacket(packet,context);
    }

    protected final void getFullRulePacket(FancyPacketMap.Mutable packet,ISyncingContext context) {
        FancyPacketMap.Mutable ruleData = FancyPacketMap.map();
        this.rules.forEach((type,rule) -> {
            FancyPacketMap.Mutable entry = FancyPacketMap.map();
            rule.createFullSyncPacket(entry,context);
            if(!entry.isEmpty())
                ruleData.setMap(type.getKey().toString(),entry);
        });
        packet.setMap("rule_update",ruleData);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void handlePacket(FancyPacketMap message) {
        super.handlePacket(message);
        this.handleRuleUpdatePacket(message);
    }

    protected final void handleRuleUpdatePacket(FancyPacketMap message) {
        if(message.contains("rule_update")) {
            FancyPacketMap ruleMap = message.getMap("rule_update");
            for(String key : ruleMap.keySet()) {
                try {
                    Identifier ruleTypeID = Identifier.parse(key);
                    TradeRuleType<?> ruleType = LCRegistries.Trader.TRADE_RULE_TYPE.getValue(ruleTypeID);
                    if(ruleType != null && this.rules.containsKey(ruleType))
                        this.rules.get(ruleType).handleSyncPacket(ruleMap.getMap(key));
                }catch (IdentifierException ignored) {}
            }
        }
    }

}