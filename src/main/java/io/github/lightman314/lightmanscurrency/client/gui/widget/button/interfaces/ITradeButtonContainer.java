package io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces;

import java.util.List;

import io.github.lightman314.lightmanscurrency.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.text.ITextComponent;

public interface ITradeButtonContainer {

	public long GetCoinValue();
	
	public IInventory GetItemInventory();
	
	public TradeEvent.TradeCostEvent TradeCostEvent(ItemTradeData trade);
	
	public boolean PermissionToTrade(int tradeIndex, List<ITextComponent> denialOutput);
	
}