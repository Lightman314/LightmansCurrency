package io.github.lightman314.lightmanscurrency.api.trader.rules;

import com.google.common.base.Predicates;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface TradeRuleHolder extends ISidedContext {

    default boolean allowTradeRule(TradeRule rule) { return true; }
    /**
     * Whether this trade rule holder only applies the rules to a specific trade
     */
    default boolean isSingleTrade() { return this instanceof TradeData; }
    /**
     * Whether this trade rule holder applies the rules to all trades across the entire trader
     */
    default boolean isMultiTrade() { return !this.isSingleTrade(); }
    default boolean canMoneyBeRelevant() { return true; }
    default boolean isMoneyRelevant() { return this.canMoneyBeRelevant(); }

    default void setRuleChanged(TradeRuleType<?> type,Consumer<FancyPacketMap.Mutable> writer) { this.setRuleChanged(type,writer, Predicates.alwaysTrue()); }
    default void setRuleChanged(TradeRuleType<?> type, Consumer<FancyPacketMap.Mutable> writer,UUID player) { this.setRuleChanged(type,writer,c -> c.isValidTarget(player)); }
    void setRuleChanged(TradeRuleType<?> type,Consumer<FancyPacketMap.Mutable> writer,Predicate<ISyncingContext> filter);

    default List<TradeRule> getRules() { return List.copyOf(this.getRuleData().values()); }
    Map<TradeRuleType<?>,TradeRule> getRuleData();
    default Map<TradeRuleType<?>,TradeRule> getEncodingRuleData() {
        Map<TradeRuleType<?>,TradeRule> result = new HashMap<>();
        this.getRuleData().forEach((type,rule) -> {
            if(rule.shouldWriteToFile())
                result.put(type,rule);
        });
        return result;
    }
    @Nullable
    default TradeRule getRuleOfType(TradeRuleType<?> type) { return this.getRuleData().get(type); }

    default void validateRuleStates(Map<TradeRuleType<?>,TradeRule> rules) {
        for(TradeRuleType<?> type : LCRegistries.Trader.TRADE_RULE_TYPE) {
            TradeRule r;
            if(rules.containsKey(type))
                r = rules.get(type);
            else
                r = type.createNew();
            if(this.allowTradeRule(r) && r.attachToHolder(this))
                rules.put(type,r);
            else
                rules.remove(type);
        }
    }
    default void handleRuleRequest(Player player,TradeRuleType<?> type, FancyPacketMap update) {
        TradeRule rule = this.getRuleOfType(type);
        if(rule != null)
            rule.handlePlayerRequest(player,update);
    }

}
