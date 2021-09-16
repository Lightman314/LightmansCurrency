package io.github.lightman314.lightmanscurrency.client.gui.screen;

import io.github.lightman314.lightmanscurrency.tradedata.rules.ITradeRuleHandler;

public interface ITradeRuleScreenHandler {

	public ITradeRuleHandler ruleHandler();
	
	public void reopenLastScreen();
	
}
