package io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces;

import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.world.Container;

public interface ITradeButtonStockSource {

	public Container getStorage();
	
	public CoinValue getStoredMoney();
	
	public boolean isCreative();
	
}
