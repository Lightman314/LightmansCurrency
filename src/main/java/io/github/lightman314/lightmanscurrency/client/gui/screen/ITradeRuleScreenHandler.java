package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.List;

import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;

public interface ITradeRuleScreenHandler {

	public ITradeRuleHandler ruleHandler();
	
	public void reopenLastScreen();
	
	public void updateServer(List<TradeRule> newRules);
	
}
