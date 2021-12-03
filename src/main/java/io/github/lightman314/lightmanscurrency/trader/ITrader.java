package io.github.lightman314.lightmanscurrency.trader;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.util.text.ITextComponent;

public interface ITrader {

	public UUID getOwnerID();
	public boolean isCreative();
	public boolean hasCustomName();
	public ITextComponent getName();
	public CoinValue getStoredMoney();
	public int getTradeCount();
	public int getTradeStock(int index);
	
}
