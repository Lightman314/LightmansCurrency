package io.github.lightman314.lightmanscurrency.api.traders.rules;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface ITradeRuleHost {

    default boolean allowTradeRule(TradeRule rule) { return true; }
    boolean isTrader();
    boolean isTrade();
    default boolean canMoneyBeRelevant() { return true; }
    default boolean isMoneyRelevant() { return this.canMoneyBeRelevant(); }
    void setRuleChanged(TradeRuleType<?> type);

    default List<TradeRule> getRules() { return ImmutableList.copyOf(this.getRuleMap().values()); }
    Map<TradeRuleType<?>,TradeRule> getRuleMap();
    @Nullable
    TradeRule addRule(TradeRuleType<?> rule);
    @Nullable
    default TradeRule getRuleOfType(TradeRuleType<?> type) { return this.getRuleMap().get(type); }

    /**
     * Should be called by the host whenever something changes with the host that could potentially change the results of ITradeRuleHost.isMoneyRelevant
     */
    default void validateRuleStates() { TradeRule.ValidateTradeRuleActiveStates(this,this.getRuleMap()); }
    default void HandleRuleUpdate(Player player,ResourceLocation type, LazyPacketData updateData)
    {
        TradeRuleType<?> t = LCRegistries.TRADE_RULE.get(type);
        TradeRule rule = this.getRuleOfType(t);
        if(rule != null)
        {
            rule.receiveUpdateMessage(player, updateData);
            this.setRuleChanged(t);
        }
    }

}
