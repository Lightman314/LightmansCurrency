package io.github.lightman314.lightmanscurrency.events;

import java.util.List;
import java.util.Objects;

import io.github.lightman314.lightmanscurrency.common.universal_traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.tradedata.AuctionTradeData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class AuctionHouseEvent extends Event {

	protected final AuctionHouseTrader auctionHouse;
	public AuctionHouseTrader getAuctionHouse() { return this.auctionHouse; }
	
	protected AuctionHouseEvent(AuctionHouseTrader auctionHouse) { this.auctionHouse = auctionHouse; }
	
	public static class AuctionEvent extends AuctionHouseEvent {
		
		protected AuctionTradeData auction;
		public AuctionTradeData getAuction() { return this.auction; }

		
		protected AuctionEvent(AuctionHouseTrader auctionHouse, AuctionTradeData auction) { super(auctionHouse); this.auction = auction; }
		
		public static class CreateAuctionEvent extends AuctionEvent {
			
			protected final boolean persistent;
			public boolean isPersistent() { return this.persistent; }
			
			protected CreateAuctionEvent(AuctionHouseTrader auctionHouse, AuctionTradeData auction, boolean persistent) {
				super(auctionHouse, auction);
				this.persistent = persistent;
			}
			
			public static final class Pre extends CreateAuctionEvent {

				public Pre(AuctionHouseTrader auctionHouse, AuctionTradeData auction, boolean persistent) { super(auctionHouse, auction, persistent); }
				
				public void setAuction(AuctionTradeData auction) {
					Objects.requireNonNull(auction);
					this.auction = auction;
				}
				
				@Override
				public boolean isCancelable() { return !this.isPersistent(); }
				
			}
			
			public static final class Post extends CreateAuctionEvent {
				public Post(AuctionHouseTrader auctionHouse, AuctionTradeData auction, boolean persistent) { super(auctionHouse, auction, persistent); }
			}
			
		}
		
		public static class CancelAuctionEvent extends AuctionEvent {

			protected final Player player;
			public Player getPlayer() { return this.player; }
			
			public CancelAuctionEvent(AuctionHouseTrader auctionHouse, AuctionTradeData auction, Player player) {
				super(auctionHouse, auction);
				this.player = player;
			}
			
		}
		
		public static class AuctionCompletedEvent extends AuctionEvent {

			public boolean hadBidder() { return this.auction.getLastBidPlayer() != null; }
			
			List<ItemStack> items;
			public List<ItemStack> getItems() { return this.items; }
			public void setItems(List<ItemStack> bidderRewards) { this.items = Objects.requireNonNull(bidderRewards); }
			
			CoinValue paymentAmount;
			public CoinValue getPayment() { return this.paymentAmount; }
			public void setPayment(CoinValue paymentAmount) { this.paymentAmount = Objects.requireNonNull(paymentAmount); }
			
			public AuctionCompletedEvent(AuctionHouseTrader auctionHouse, AuctionTradeData auction) {
				super(auctionHouse, auction);
				this.items = this.auction.getAuctionItems();
				if(this.hadBidder())
					this.paymentAmount = this.auction.getLastBidAmount();
				else
					this.paymentAmount = CoinValue.EMPTY;
			}
			
		}
		
		public static class AuctionBidEvent extends AuctionEvent {

			protected final Player bidder;
			public Player getBidder() { return this.bidder; }
			
			protected CoinValue bidAmount;
			public CoinValue getBidAmount() { return this.bidAmount; }
			
			protected AuctionBidEvent(AuctionHouseTrader auctionHouse, AuctionTradeData auction, Player bidder, CoinValue bidAmount) {
				super(auctionHouse, auction);
				this.bidder = bidder;
				this.bidAmount = bidAmount;
			}
			
			@Cancelable
			public static class Pre extends AuctionBidEvent {

				public void setBidAmount(CoinValue bidAmount) { this.bidAmount = Objects.requireNonNull(bidAmount); }
				
				public Pre(AuctionHouseTrader auctionHouse, AuctionTradeData auction, Player bidder, CoinValue bidAmount) {
					super(auctionHouse, auction, bidder, bidAmount);
				}
				
			}
			
			public static class Post extends AuctionBidEvent {
				
				public Post(AuctionHouseTrader auctionHouse, AuctionTradeData auction, Player bidder,CoinValue bidAmount) {
					super(auctionHouse, auction, bidder, bidAmount);
				}
			}
			
		}
		
	}
	
}
