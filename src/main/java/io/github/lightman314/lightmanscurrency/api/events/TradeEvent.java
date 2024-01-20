package io.github.lightman314.lightmanscurrency.api.events;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;

public abstract class TradeEvent extends Event{

	private final PlayerReference player;
	@Nonnull
	public final PlayerReference getPlayerReference() { return this.player; }
	private final TradeData trade;
	@Nonnull
	public final TradeData getTrade() { return this.trade; }
	public final int getTradeIndex() { return this.trader.indexOfTrade(this.trade); }
	private final TraderData trader;
	@Nonnull
	public final TraderData getTrader() { return this.trader; }
	
	protected TradeEvent(@Nonnull PlayerReference player, @Nonnull TradeData trade, @Nonnull TraderData trader)
	{
		this.player = player;
		this.trade = trade;
		this.trader = trader;
	}
	
	@Cancelable
	public static class PreTradeEvent extends TradeEvent
	{
		
		private final List<AlertData> alerts = new ArrayList<>();
		
		public PreTradeEvent(@Nonnull PlayerReference player, @Nonnull TradeData trade, @Nonnull TraderData trader) { super(player, trade, trader); }
		
		/**
		 * Adds an alert to the trade display.
		 * Use addHelpful, addWarning, addError, or addDenial for easier to use templates if you don't wish to add any special formatting to your alert.
		 * @param cancelTrade Whether to also cancel the trade/event.
		 */
		public void addAlert(@Nonnull AlertData alert, boolean cancelTrade) {
			this.alerts.add(alert);
			if(cancelTrade)
				this.setCanceled(true);
		}
		
		/**
		 * Adds an alert to the trade with default helpful formatting (Green).
		 * Does not cancel the trade.
		 */
		public void addHelpful(@Nonnull MutableComponent message) {
			this.addAlert(AlertData.helpful(message), false);
		}

		public void addNeutral(@Nonnull MutableComponent message) { this.addAlert(AlertData.neutral(message), false);}

		/**
		 * Adds an alert to the trade with default warning formatting (Orange).
		 * Does not cancel the trade.
		 */
		public void addWarning(@Nonnull MutableComponent message) { this.addAlert(AlertData.warn(message), false); }
		
		/**
		 * Adds an alert to the trade with default error formatting (Red).
		 * Does not cancel the trade.
		 * Use addDenial if you wish to cancel the trade.
		 */
		public void addError(@Nonnull MutableComponent message) {
			this.addAlert(AlertData.error(message), false);
		}
		
		/**
		 * Adds an alert to the trade with default error formatting (Red).
		 * Also cancels the trade.
		 * Use addError if you do not wish to cancel the trade.
		 */
		public void addDenial(@Nonnull MutableComponent message) { this.addAlert(AlertData.error(message), true); }

		@Nonnull
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

		MoneyValue baseCost;
		public MoneyValue getBaseCost() { return this.baseCost; }
		public MoneyValue getCostResult() { return this.forceFree ? MoneyValue.free() : this.baseCost.percentageOfValue(this.pricePercentage); }
		
		public TradeCostEvent(@Nonnull PlayerReference player, @Nonnull TradeData trade, @Nonnull TraderData trader)
		{
			super(player, trade, trader);
			this.pricePercentage = 100;
			this.baseCost = trade.getCost();
		}

		public final boolean matches(@Nonnull TradeData trade) { return this.getTrade() == trade; }

		public final boolean matches(@Nonnull TradeCostEvent event) { return this.getTrade() == event.getTrade() && this.forceFree == event.forceFree && this.pricePercentage == event.pricePercentage && this.baseCost.equals(event.baseCost); }

	}
	
	public static class PostTradeEvent extends TradeEvent
	{
		
		private boolean isDirty = false;
		private final MoneyValue pricePaid;
		public MoneyValue getPricePaid() { return this.pricePaid; }
		private final MoneyValue taxesPaid;
		public MoneyValue getTaxesPaid() { return this.taxesPaid; }
		
		public PostTradeEvent(PlayerReference player, TradeData trade, TraderData trader, MoneyValue pricePaid, MoneyValue taxesPaid)
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
