package io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces;

import java.util.List;

import io.github.lightman314.lightmanscurrency.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;

public interface ITradeButtonContainer {

	public long GetCoinValue();
	
	public Container GetItemInventory();
	
	public TradeEvent.TradeCostEvent TradeCostEvent(ItemTradeData trade);
	
	public boolean PermissionToTrade(int tradeIndex, List<Component> denialOutput);
	
}
