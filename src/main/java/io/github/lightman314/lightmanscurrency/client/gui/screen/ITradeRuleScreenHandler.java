package io.github.lightman314.lightmanscurrency.client.gui.screen;

import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface ITradeRuleScreenHandler {

	public ITradeRuleHandler ruleHandler();
	
	public void reopenLastScreen();
	
	public void updateServer(ResourceLocation type, CompoundNBT updateInfo);
	
	public default boolean stillValid() { return this.ruleHandler() != null; }
	
}
