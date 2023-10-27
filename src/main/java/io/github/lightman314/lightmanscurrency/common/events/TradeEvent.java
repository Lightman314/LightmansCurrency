package io.github.lightman314.lightmanscurrency.common.events;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class TradeEvent extends Event{

	private final PlayerReference player;
	public final PlayerReference getPlayerReference() { return this.player; }
	@Deprecated //Use getPlayerReference when possible
	public final Player getPlayer() { return this.player.getPlayer(); }
	private final TradeData trade;
	public final TradeData getTrade() { return this.trade; }
	public final int getTradeIndex() { return this.trader.indexOfTrade(this.trade); }
	private final TraderData trader;
	public final TraderData getTrader() { return this.trader; }
	
	protected TradeEvent(PlayerReference player, TradeData trade, TraderData trader)
	{
		this.player = player;
		this.trade = trade;
		this.trader = trader;
	}
	
	@Cancelable
	public static class PreTradeEvent extends TradeEvent
	{
		
		private final List<AlertData> alerts = new ArrayList<>();
		
		public PreTradeEvent(PlayerReference player, TradeData trade, TraderData trader)
		{
			super(player, trade, trader);
		}
		
		/**
		 * Adds an alert to the trade display.
		 * Use addHelpful, addWarning, addError, or addDenial for easier to use templates if you don't wish to add any special formatting to your alert.
		 * @param cancelTrade Whether to also cancel the trade/event.
		 */
		public void addAlert(AlertData alert, boolean cancelTrade) {
			this.alerts.add(alert);
			if(cancelTrade)
				this.setCanceled(true);
		}
		
		/**
		 * Adds an alert to the trade with default helpful formatting (Green).
		 * Does not cancel the trade.
		 */
		public void addHelpful(MutableComponent message) {
			this.addAlert(AlertData.helpful(message), false);
		}
		
		/**
		 * Adds an alert to the trade with default warning formatting (Orange).
		 * Does not cancel the trade.
		 */
		public void addWarning(MutableComponent message) { this.addAlert(AlertData.warn(message), false); }
		
		/**
		 * Adds an alert to the trade with default error formatting (Red).
		 * Does not cancel the trade.
		 * Use addDenial if you wish to cancel the trade.
		 */
		public void addError(MutableComponent message) {
			this.addAlert(AlertData.error(message), false);
		}
		
		/**
		 * Adds an alert to the trade with default error formatting (Red).
		 * Also cancels the trade.
		 * Use addError if you do not wish to cancel the trade.
		 */
		public void addDenial(MutableComponent message) { this.addAlert(AlertData.error(message), true); }

		public List<AlertData> getAlertInfo() { return this.alerts; }
		
		
	}
	
	public static class TradeCostEvent extends TradeEvent
	{

		private boolean forceFree = false;
		public boolean forcedFree() { return this.forceFree; }
		public void makeFree() { this.forceFree = true; }
		public void makeNotFree() { this.forceFree = false; }
		private int pricePercentage;
		public int getPricePercentage() { return this.pricePercentage; }
		public void setPricePercentage(int pricePercentage) { this.pricePercentage = pricePercentage; }
		public void giveDiscount(int percentage) { this.pricePercentage -=percentage; }
		public void hikePrice(int percentage) { this.pricePercentage += percentage; }

		CoinValue currentCost;
		public CoinValue getBaseCost() { return this.currentCost; }
		public CoinValue getCostResult() { return this.forceFree ? CoinValue.FREE : this.currentCost.percentageOfValue(this.pricePercentage); }
		
		public TradeCostEvent(PlayerReference player, TradeData trade, TraderData trader)
		{
			super(player, trade, trader);
			this.pricePercentage = 100;
			this.currentCost = trade.getCost();
		}
	}
	
	public static class PostTradeEvent extends TradeEvent
	{
		
		private boolean isDirty = false;
		private final CoinValue pricePaid;
		public CoinValue getPricePaid() { return this.pricePaid; }
		private final CoinValue taxesPaid;
		public CoinValue getTaxesPaid() { return this.taxesPaid; }
		
		public PostTradeEvent(PlayerReference player, TradeData trade, TraderData trader, CoinValue pricePaid, CoinValue taxesPaid)
		{
			super(player, trade, trader);
			this.pricePaid = pricePaid;
			this.taxesPaid = taxesPaid;
		}
		
		public boolean isDirty() { return this.isDirty; }
		
		public void markDirty() { this.isDirty = true; }
	 		
		public void clean() { this.isDirty = false; }
		
	}
	
}
