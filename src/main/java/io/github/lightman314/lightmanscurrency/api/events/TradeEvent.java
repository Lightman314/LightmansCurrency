package io.github.lightman314.lightmanscurrency.api.events;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TradeEvent extends Event{

	public final boolean hasPlayerReference() { return this.context.hasPlayerReference(); }
	/**
	 * Player Reference is {@link Nullable} as the {@link PreTradeEvent} & {@link TradeCostEvent} Events
	 * are also called from the storage context which will does include a player reference in the trades context
	 */
	@Nullable
	public final PlayerReference getPlayerReference() { return this.context.getPlayerReference(); }
	private final TradeData trade;
	public final TradeData getTrade() { return this.trade; }
	public final int getTradeStock() { return this.trade.getStock(this.context); }
	public final int getTradeIndex() { return this.getTrader().indexOfTrade(this.trade); }
	public final TraderData getTrader() { return this.context.getTrader(); }
	private final TradeContext context;
	
	public final TradeContext getContext() { return this.context; }
	
	protected TradeEvent(TradeData trade, TradeContext context)
	{
		this.trade = trade;
		this.context = context;
	}
	
	@Cancelable
	public static class PreTradeEvent extends TradeEvent
	{
		
		private final List<AlertData> alerts = new ArrayList<>();
		
		public PreTradeEvent(TradeData trade, TradeContext context) { super(trade,context); }
		
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

		public void addNeutral(MutableComponent message) { this.addAlert(AlertData.neutral(message), false);}

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

		private final MoneyValue baseCost;
		
		public MoneyValue getBaseCost() { return this.baseCost; }
		public boolean getCostResultIsFree() { return this.forceFree || this.pricePercentage <= 0 || this.baseCost.isFree(); }
		
		public MoneyValue getCostResult() { return this.getCostResultIsFree() ? MoneyValue.free() : this.baseCost.percentageOfValue(this.pricePercentage); }

		@Deprecated(since = "2.2.2.5")
		public TradeCostEvent(TradeData trade, TradeContext context) { this(trade,context, TradeRule.getBaseCost(trade,context)); }
		public TradeCostEvent(TradeData trade, TradeContext context, MoneyValue baseCost)
		{
			super(trade, context);
			this.pricePercentage = 100;
			this.baseCost = baseCost;
		}

		public final boolean matches(TradeData trade) { return this.getTrade() == trade; }

		public final boolean matches(TradeCostEvent event) { return this.getTrade() == event.getTrade() && this.forceFree == event.forceFree && this.pricePercentage == event.pricePercentage && this.baseCost.equals(event.baseCost); }

	}
	
	public static class PostTradeEvent extends TradeEvent
	{
		
		private boolean isDirty = false;
		private final MoneyValue pricePaid;
		public MoneyValue getPricePaid() { return this.pricePaid; }
		private final MoneyValue taxesPaid;
		public MoneyValue getTaxesPaid() { return this.taxesPaid; }
        private final List<?> product;
        public List<?> getProduct() { return ImmutableList.copyOf(this.product); }
		
		public PostTradeEvent(TradeData trade, TradeContext context, MoneyValue pricePaid, MoneyValue taxesPaid) { this(trade,context,pricePaid,taxesPaid,new ArrayList<>()); }
		public PostTradeEvent(TradeData trade, TradeContext context, MoneyValue pricePaid, MoneyValue taxesPaid, List<?> product)
		{
			super(trade, context);
			this.pricePaid = pricePaid;
			this.taxesPaid = taxesPaid;
            this.product = product;
		}
		
		public boolean isDirty() { return this.isDirty; }
		
		public void markDirty() { this.isDirty = true; }
	 		
		public void clean() { this.isDirty = false; }
		
	}
	
}
