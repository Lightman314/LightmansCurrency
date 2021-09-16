package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.List;

import io.github.lightman314.lightmanscurrency.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.tradedata.rules.TradeRule;

public interface ITradeRuleScreenHandler {

	public ITradeRuleHandler ruleHandler();
	
	public void reopenLastScreen();
	
	public void updateServer(List<TradeRule> newRules);
	
}
