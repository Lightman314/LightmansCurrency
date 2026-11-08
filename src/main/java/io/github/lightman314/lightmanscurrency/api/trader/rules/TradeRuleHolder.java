package io.github.lightman314.lightmanscurrency.api.trader.rules;

import com.google.common.base.Predicates;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.persistent.PersistentDataException;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.*;
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
    default int getValidTradeCount() { return this instanceof INodeAccess trader ? TradingNode.getCustomerReadyTradeCount(trader) : 1; }
    default boolean canPotentialPriceBeModified() { return true; }
    default boolean canCurrentPriceBeModified() { return this.canPotentialPriceBeModified(); }

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

    /**
     * Used internally to remove unecessary data for a persistent trader
     * @param rule The trade rule to be removed
     */
    @ApiStatus.Internal
    void removeUnusedRule(TradeRule rule);

    void validateRuleStates();
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

    static void writePersistentRuleData(TradeRuleHolder holder,ValueOutput output) {
        holder.getRules().forEach(rule -> {
            if(rule.isActive() && rule instanceof IPersistentRule pr) {
                try {
                    String type = rule.getType().getKey().toString();
                    ValueOutput entry = output.child(type);
                    pr.writePersistentData(entry);
                    if(entry.isEmpty())
                        output.discard(type);
                } catch (PersistentDataException ignored) {}
            }
        });
    }

    static void readPersistentRuleData(TradeRuleHolder holder, ValueInput input,ProblemReporter reporter) {
        holder.validateRuleStates();
        for(TradeRule rule : new ArrayList<>(holder.getRules())) {
            boolean remove = false;
            if(rule instanceof IPersistentRule pr) {
                String key = rule.getType().getKey().toString();
                ProblemReporter entryReporter = reporter.forChild(new ProblemReporter.FieldPathElement(key));
                input.child(key).ifPresent(entry -> {
                    try {
                        pr.loadPersistentData(entry);
                        rule.setActive(true);
                    } catch (PersistentDataException e) {
                        holder.removeUnusedRule(rule);
                        entryReporter.report(e);
                    }
                });
            }
            else
                holder.removeUnusedRule(rule);
        }
    }

    static void writePersistentRuleSaveData(TradeRuleHolder holder,ValueOutput output) {
        for(TradeRule rule : holder.getRules()) {
            if(rule instanceof IPersistentRule pr) {
                String type = rule.getType().getKey().toString();
                ValueOutput entry = output.child(type);
                pr.writePersistentSaveData(entry);
                if(entry.isEmpty())
                    output.discard(type);
            }
        }
    }

    static void readPersistentRuleSaveData(TradeRuleHolder holder,ValueInput input) {
        for(TradeRule rule : holder.getRules()) {
            if(rule instanceof IPersistentRule pr)
                input.child(rule.getType().getKey().toString()).ifPresent(pr::readPersisentSaveData);
        }
    }

}
