package io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces;

import io.github.lightman314.lightmanscurrency.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import net.minecraft.inventory.IInventory;

public interface ITradeButtonContainer {

	public long GetCoinValue();
	
	public IInventory GetItemInventory();
	
	public TradeEvent.TradeCostEvent TradeCostEvent(ItemTradeData trade);
	
	public boolean PermissionToTrade(int tradeIndex);
	
	public ItemTradeData GetTrade(int tradeIndex);
	
}
