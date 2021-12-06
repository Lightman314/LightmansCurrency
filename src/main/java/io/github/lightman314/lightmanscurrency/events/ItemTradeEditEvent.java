package io.github.lightman314.lightmanscurrency.events;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.world.item.ItemStack;

public class ItemTradeEditEvent extends TradeEditEvent{

	public final IItemTrader getItemTrader()
	{
		ITrader t = this.getTrader();
		if(t instanceof IItemTrader)
			return (IItemTrader)t;
		return null;
	}
	
	public final ItemTradeData getTrade() { if(this.getItemTrader() != null) { return this.getItemTrader().getTrade(this.tradeIndex); } return null; }
	
	public ItemTradeEditEvent(Supplier<ITrader> trader, int tradeIndex) {
		super(trader, tradeIndex);
	}
	
	public static class ItemTradeItemEditEvent extends ItemTradeEditEvent
	{
		
		private final ItemStack oldItem;
		public final ItemStack getOldItem() { return this.oldItem; }
		private final int slot;
		public final int getSlot() { return this.slot; }
		
		public ItemTradeItemEditEvent(Supplier<ITrader> trader, int tradeIndex, ItemStack oldItem, int slot)
		{
			super(trader, tradeIndex);
			this.oldItem = oldItem;
			this.slot = slot;
		}
	}

}
