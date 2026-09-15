package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.TradeRulesNode;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class GlobalTradeRuleTab extends AbstractTradeRuleTab {

    public static final Identifier KEY = LCApi.id("global_trade_rules");

    public GlobalTradeRuleTab(TraderStorageMenu menu) { super(menu); }

    @Nullable
    @Override
    public TradeRuleHolder getRuleHolder() { return this.getNode(TradeRulesNode.TYPE); }
    @Override
    public Identifier getKey() { return KEY; }

}
