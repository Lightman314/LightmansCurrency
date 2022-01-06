package io.github.lightman314.lightmanscurrency.trader;

import java.util.List;

import io.github.lightman314.lightmanscurrency.tileentity.IPermissions;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;

public interface ITrader extends IPermissions{

	public CoinValue getStoredMoney();
	public int getTradeCount();
	public int getTradeCountLimit();
	public int getTradeStock(int index);
	public CoreTraderSettings getCoreSettings();
	public void markCoreSettingsDirty();
	public List<Settings> getAdditionalSettings();
	public void requestAddOrRemoveTrade(boolean isAdd);
	public void addTrade(PlayerEntity requestor);
	public void removeTrade(PlayerEntity requestor);
	
}
