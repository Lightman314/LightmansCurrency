package io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces;

import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.inventory.IInventory;

public interface ITradeButtonStockSource {

	public IInventory getStorage();
	
	public CoinValue getStoredMoney();
	
	public boolean isCreative();
	
}
