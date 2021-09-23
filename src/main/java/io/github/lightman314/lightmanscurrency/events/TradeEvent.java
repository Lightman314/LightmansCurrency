package io.github.lightman314.lightmanscurrency.events;

import io.github.lightman314.lightmanscurrency.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.eventbus.api.Event;

public abstract class TradeEvent extends Event{

	private final PlayerEntity player;
	public final PlayerEntity getPlayer() { return this.player; }
	private final TradeData trade;
	public final TradeData getTrade() { return this.trade; }
	private final Container container;
	public final Container getContainer() { return this.container; }
	
	protected TradeEvent(PlayerEntity player, TradeData trade, Container container)
	{
		this.player = player;
		this.trade = trade;
		this.container = container;
	}
	
	public static class PreTradeEvent extends TradeEvent
	{
		public PreTradeEvent(PlayerEntity player, TradeData trade, Container container)
		{
			super(player, trade, container);
		}
		
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
		
		public TradeCostEvent(PlayerEntity player, TradeData trade, Container container)
		{
			super(player, trade, container);
			this.costMultiplier = 1f;
			this.currentCost = trade.getCost();
		}
	}
	
	public static class PostTradeEvent extends TradeEvent
	{
		
		private boolean isDirty = false;
		
		public PostTradeEvent(PlayerEntity player, TradeData trade, Container container)
		{
			super(player, trade, container);
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
