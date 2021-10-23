package io.github.lightman314.lightmanscurrency.events;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;

public abstract class TradeEvent extends Event{

	private final PlayerEntity player;
	public final PlayerEntity getPlayer() { return this.player; }
	private final TradeData trade;
	public final TradeData getTrade() { return this.trade; }
	private final Container container;
	public final Container getContainer() { return this.container; }
	private final Supplier<ITrader> traderSource;
	public final ITrader getTrader() { return this.traderSource.get(); }
	
	protected TradeEvent(PlayerEntity player, TradeData trade, Container container, Supplier<ITrader> trader)
	{
		this.player = player;
		this.trade = trade;
		this.container = container;
		this.traderSource = trader;
	}
	
	public static class PreTradeEvent extends TradeEvent
	{
		
		private final List<ITextComponent> denialText = Lists.newArrayList();
		
		public PreTradeEvent(PlayerEntity player, TradeData trade, Container container, Supplier<ITrader> trader)
		{
			super(player, trade, container, trader);
		}
		
		public void denyTrade(ITextComponent reason)
		{
			denialText.add(reason);
			this.setCanceled(true);
		}
		
		public List<ITextComponent> getDenialReasons() { return this.denialText; }
		
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
		
		public TradeCostEvent(PlayerEntity player, TradeData trade, Container container, Supplier<ITrader> trader)
		{
			super(player, trade, container, trader);
			this.costMultiplier = 1f;
			this.currentCost = trade.getCost();
		}
	}
	
	public static class PostTradeEvent extends TradeEvent
	{
		
		private boolean isDirty = false;
		private final CoinValue pricePaid;
		public CoinValue getPricePaid() { return this.pricePaid; }
		
		public PostTradeEvent(PlayerEntity player, TradeData trade, Container container, Supplier<ITrader> trader, CoinValue pricePaid)
		{
			super(player, trade, container, trader);
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
