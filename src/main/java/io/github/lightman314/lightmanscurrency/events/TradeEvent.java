package io.github.lightman314.lightmanscurrency.events;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public abstract class TradeEvent extends Event{

	private final PlayerReference player;
	public final PlayerReference getPlayerReference() { return this.player; }
	@Deprecated //Use getPlayerReference when possible
	public final Player getPlayer() { return this.player.getPlayer(); }
	private final TradeData trade;
	public final TradeData getTrade() { return this.trade; }
	private final ITrader trader;
	public final ITrader getTrader() { return this.trader; }
	
	protected TradeEvent(PlayerReference player, TradeData trade, ITrader trader)
	{
		this.player = player;
		this.trade = trade;
		this.trader = trader;
	}
	
	public static class PreTradeEvent extends TradeEvent
	{
		
		private final List<Component> denialText = Lists.newArrayList();
		
		public PreTradeEvent(PlayerReference player, TradeData trade, ITrader trader)
		{
			super(player, trade, trader);
		}
		
		public void denyTrade(Component reason)
		{
			denialText.add(reason);
			this.setCanceled(true);
		}
		
		public List<Component> getDenialReasons() { return this.denialText; }
		
		@Override
		public boolean isCancelable() { return true; }
		
		
	}
	
	public static class TradeCostEvent extends TradeEvent
	{
		
		private double costMultiplier;
		public double getCostMultiplier() { return this.costMultiplier; }
		public void applyCostMultiplier(double newCostMultiplier) { this.costMultiplier = MathUtil.clamp(this.costMultiplier * newCostMultiplier, 0d, 2d); }
		public void setCostMultiplier(double newCostMultiplier) { this.costMultiplier = MathUtil.clamp(newCostMultiplier, 0d, 2d); }
		
		CoinValue currentCost;
		public CoinValue getBaseCost() { return this.currentCost; }
		public CoinValue getCostResult() { return this.currentCost.ApplyMultiplier(this.costMultiplier); }
		
		public TradeCostEvent(PlayerReference player, TradeData trade, ITrader trader)
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
		public CoinValue getPricePaid() { return this.pricePaid; }
		
		public PostTradeEvent(PlayerReference player, TradeData trade, ITrader trader, CoinValue pricePaid)
		{
			super(player, trade, trader);
			this.pricePaid = pricePaid;
		}
		
		public boolean isDirty()
		{
			return this.isDirty;
		}
		
		public void markDirty()
		{
			this.isDirty = true;
		}
		
		public void clean()
		{
			this.isDirty = false;
		}
		
	}
	
}
