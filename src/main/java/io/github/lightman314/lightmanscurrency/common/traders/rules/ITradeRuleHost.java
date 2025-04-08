package io.github.lightman314.lightmanscurrency.common.traders.rules;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface ITradeRuleHost {

    default boolean allowTradeRule(TradeRule rule) { return true; }
    boolean isTrader();
    boolean isTrade();
    default boolean canMoneyBeRelevant() { return true; }
    default boolean isMoneyRelevant() { return this.canMoneyBeRelevant(); }
    void markTradeRulesDirty();
    
    List<TradeRule> getRules();
    default TradeRule getRuleOfType(ResourceLocation ruleType) { return TradeRule.GetTradeRule(this.getRules(), ruleType); }

    /**
     * Should be called by the host whenever something changes with the host that could potentially change the results of ITradeRuleHost.isMoneyRelevant
     */
    default void validateRuleStates() { TradeRule.ValidateTradeRuleActiveStates(this.getRules()); }
    default void HandleRuleUpdate(Player player,ResourceLocation type, LazyPacketData updateData)
    {
        TradeRule rule = this.getRuleOfType(type);
        if(rule != null)
        {
            rule.receiveUpdateMessage(player,updateData);
            this.markTradeRulesDirty();
        }
    }

}
