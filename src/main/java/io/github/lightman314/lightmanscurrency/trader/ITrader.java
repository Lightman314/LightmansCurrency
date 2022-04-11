package io.github.lightman314.lightmanscurrency.trader;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.lightman314.lightmanscurrency.blockentity.interfaces.IPermissions;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.common.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public interface ITrader extends IPermissions, ITraderSource {

	/**
	 * The name of the trader.
	 */
	public Component getName();
	/**
	 * The formatted name & owner of the trader
	 */
	public Component getTitle();
	public CoinValue getStoredMoney();
	public CoinValue getInternalStoredMoney();
	public void addStoredMoney(CoinValue amount);
	public void removeStoredMoney(CoinValue amount);
	public void clearStoredMoney();
	public void markMoneyDirty();
	public int getTradeCount();
	public int getTradeCountLimit();
	public void markTradesDirty();
	public int getTradeStock(int index);
	//Menu stuff
	public void openTradeMenu(Player player);
	public void openStorageMenu(Player player);
	//Settings stuff
	public CoreTraderSettings getCoreSettings();
	public void markCoreSettingsDirty();
	public List<Settings> getAdditionalSettings();
	public default Map<String,Integer> getAllyDefaultPermissions() { return Maps.newHashMap(); }
	//Creative stuff
	public default boolean isCreative() { return this.getCoreSettings().isCreative(); }
	public void requestAddOrRemoveTrade(boolean isAdd);
	public void addTrade(Player requestor);
	public void removeTrade(Player requestor);
	//Client Check
	public boolean isClient();
	public default boolean isServer() { return !this.isClient(); }
	//Menu Functions
	public void userOpen(Player player);
	public void userClose(Player player);
	//Client-side messages
	public void sendOpenTraderMessage();
	public void sendOpenStorageMessage();
	public void sendClearLogMessage();
	//Permission stuff
	public default int getPermissionLevel(Player player, String permission) {
		return this.getCoreSettings().getPermissionLevel(player, permission);
	}
	public default int getPermissionLevel(PlayerReference player, String permission) {
		return this.getCoreSettings().getPermissionLevel(player, permission);
	}
	//Trade Rule stuff
	public ITradeRuleScreenHandler getRuleScreenHandler(int tradeIndex);
	
	//Trade Events
	default PreTradeEvent runPreTradeEvent(PlayerReference player, TradeData trade)
	{
		PreTradeEvent event = new PreTradeEvent(player, trade, this);
		trade.beforeTrade(event);
		if(this instanceof ITradeRuleHandler)
			((ITradeRuleHandler)this).beforeTrade(event);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
	
	default TradeCostEvent runTradeCostEvent(PlayerReference player, TradeData trade)
	{
		TradeCostEvent event = new TradeCostEvent(player, trade, this);
		trade.tradeCost(event);
		if(this instanceof ITradeRuleHandler)
			((ITradeRuleHandler)this).tradeCost(event);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
	
	default void runPostTradeEvent(PlayerReference player, TradeData trade, CoinValue pricePaid)
	{
		PostTradeEvent event = new PostTradeEvent(player, trade, this, pricePaid);
		trade.afterTrade(event);
		if(event.isDirty())
		{
			this.markTradesDirty();
			event.clean();
		}
		if(this instanceof ITradeRuleHandler)
		{
			((ITradeRuleHandler)this).afterTrade(event);
			if(event.isDirty())
			{
				((ITradeRuleHandler)this).markRulesDirty();
				event.clean();
			}
		}
		MinecraftForge.EVENT_BUS.post(event);
	}
	
	public default boolean canInteractRemotely() { return false; }
	
	public TradeResult ExecuteTrade(TradeContext context, int tradeIndex);
	
	public default List<ITrader> getTraders() { return Lists.newArrayList(this); }
	public default boolean isSingleTrader() { return true; }
	
	//Display Stuff
	/**
	 * List of trades used for display purposes on the trader screen.
	 * Indexes should match the trade indexes of your trades.
	 * No need to filter out invalid trades, as trade.isValid() will be run before displaying a trade (unless the trader is in storage mode).
	 */
	public List<? extends ITradeData> getTradeInfo();
	
	/**
	 * Adds interaction slots to the menu.
	 * Make sure to confirm that another trader of the same type hasn't already added the interaction slot you wish to add.
	 */
	public default void addInteractionSlots(List<InteractionSlotData> interactionSlots) { }
	
	/**
	 * Used to create the default storage tabs for the traders storage menu.
	 * If not changed, the default BasicTradeEditTab will be the only available tab.
	 */
	public default void initStorageTabs(TraderStorageMenu menu) { }
	
}
