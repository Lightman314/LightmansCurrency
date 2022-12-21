package io.github.lightman314.lightmanscurrency.events;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.network.chat.Component;
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
		 * @deprecated Use addAlert instead.
		 */
		@Deprecated
		public void denyTrade(Component reason) { this.addAlert(AlertData.convert(reason), true); }
		
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
		public void addWarning(MutableComponent message) {
			this.addAlert(AlertData.warn(message), false);
		}
		
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
		 * Use addError if you do not with so cancel the trade.
		 */
		public void addDenial(MutableComponent message) {
			this.addAlert(AlertData.error(message), true);
		}
		
		/**
		 * @deprecated use getAlertInfo instead.
		 */
		@Deprecated
		public List<Component> getDenialReasons() { List<Component> text = new ArrayList<>(); for(AlertData a : this.alerts) text.add(a.getFormattedMessage()); return text; }
		
		public List<AlertData> getAlertInfo() { return this.alerts; }
		
		
	}
	
	public static class TradeCostEvent extends TradeEvent
	{
		
		private double costMultiplier;
		public double getCostMultiplier() { return this.costMultiplier; }
		public void applyCostMultiplier(double newCostMultiplier) { this.costMultiplier = MathUtil.clamp(this.costMultiplier * newCostMultiplier, 0d, 2d); }
		public void setCostMultiplier(double newCostMultiplier) { this.costMultiplier = MathUtil.clamp(newCostMultiplier, 0d, 2d); }
		
		CoinValue currentCost;
		public CoinValue getBaseCost() { return this.currentCost.copy(); }
		public CoinValue getCostResult() { return this.currentCost.ApplyMultiplier(this.costMultiplier); }
		
		public TradeCostEvent(PlayerReference player, TradeData trade, TraderData trader)
		{
			super(player, trade, trader);
			this.costMultiplier = 1f;
			this.currentCost = trade.getCost();
		}
	}
	
	public static class PostTradeEvent extends TradeEvent
	{
		
		private boolean isDirty = false;
		private final CoinValue pricePaid;
		public CoinValue getPricePaid() { return this.pricePaid.copy(); }
		
		public PostTradeEvent(PlayerReference player, TradeData trade, TraderData trader, CoinValue pricePaid)
		{
			super(player, trade, trader);
			this.pricePaid = pricePaid;
		}
		
		public boolean isDirty() { return this.isDirty; }
		
		public void markDirty() { this.isDirty = true; }
	 		
		public void clean() { this.isDirty = false; }
		
	}
	
}
