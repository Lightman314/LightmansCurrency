package io.github.lightman314.lightmanscurrency.common.traders.auction.tabs;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.client.tabs.AuctionTradeCancelClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.nodes.AuctionTradesNode;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class AuctionTradeCancelTab extends TraderStorageNodeTab<AuctionTradesNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("auction/cancel_auction");

	public AuctionTradeCancelTab(ITraderStorageMenu menu) { super(AuctionTradesNode.TYPE,menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

	@Override
	public Object createClientTab(Object screen) { return new AuctionTradeCancelClientTab(screen, this); }
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
    @Nullable
	public AuctionTradeData getTrade() {
        AuctionTradesNode node = this.getNode();
		if(node != null)
		{
			if(this.tradeIndex >= node.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.ChangeTab(0);
				return null;
			}
            return node.getTrade(this.tradeIndex);
		}
		return null;
	}
	
	public void cancelAuction(boolean giveToPlayer) {
        TraderData trader = this.menu.getTrader();
		AuctionTradesNode node = this.getNode();
		if(node != null && trader != null)
		{
			AuctionTradeData trade = this.getTrade();
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder().setBoolean("CancelAuction", giveToPlayer));
				//Don't run the cancel interaction while on the client
				return;
			}
			if(trade.isOwner(this.menu.getPlayer()))
			{
				trade.CancelTrade(trader, giveToPlayer, this.menu.getPlayer());
				this.menu.SendMessage(this.builder().setBoolean("CancelSuccess", true));
			}
		}
	}

	@Override
	public void OpenMessage(LazyPacketData message) {
		if(message.contains("TradeIndex"))
			this.tradeIndex = message.getInt("TradeIndex");
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("CancelAuction"))
		{
			this.cancelAuction(message.getBoolean("CancelAuction"));
		}
	}

}
