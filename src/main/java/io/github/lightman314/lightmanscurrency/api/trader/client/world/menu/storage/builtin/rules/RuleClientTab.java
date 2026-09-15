package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.client.ClientPairedRegistry;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public abstract class RuleClientTab<T extends TradeRule> extends TradeRuleClientSubTab {

    public static final ClientPairedRegistry<TradeRuleType<?>,Function<TradeRulesClientTab,RuleClientTab<?>>> TAB_BUILDERS = new ClientPairedRegistry<>(LCRegistries.Trader.TRADE_RULE_TYPE,t -> null);

    public RuleClientTab(TradeRulesClientTab tab) { super(tab); }

    @Override
    public IconData getIcon() { return getRule().getIcon(); }

    @Override
    public Component getName() { return this.getRule().getName(); }

    @Override
    public boolean isVisible() {
        T rule = this.getRule();
        return rule != null && rule.isActive();
    }

    public abstract TradeRuleType<T> getType();

    public final boolean hasRule() { return this.getCommonTab().hasRule(this.getType()); }

    public final T getRule() { return this.getCommonTab().getRule(this.getType()); }

    /**
     * Use {@link #requestChange(FancyPacketMap)} to request trade rule changes
     */
    @Override
    @Deprecated
    public void sendMessage(FancyPacketMap message) { }

    public final void requestChange(FancyPacketMap request) { this.getCommonTab().requestRuleChange(this.getType(),request); }

}
