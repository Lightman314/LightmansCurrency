package io.github.lightman314.lightmanscurrency.trader;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import io.github.lightman314.lightmanscurrency.tileentity.IPermissions;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

public interface ITrader extends IPermissions{

	public ITextComponent getName();
	public ITextComponent getTitle();
	public CoinValue getStoredMoney();
	public int getTradeCount();
	public int getTradeCountLimit();
	public int getTradeStock(int index);
	//Settings stuff
	public CoreTraderSettings getCoreSettings();
	public void markCoreSettingsDirty();
	public List<Settings> getAdditionalSettings();
	public default Map<String,Integer> getAllyDefaultPermissions() { return Maps.newHashMap(); }
	//Creative stuff
	public void requestAddOrRemoveTrade(boolean isAdd);
	public void addTrade(PlayerEntity requestor);
	public void removeTrade(PlayerEntity requestor);
	//Client check
	public boolean isClient();
	public default boolean isServer() { return !this.isClient(); }
	
}
