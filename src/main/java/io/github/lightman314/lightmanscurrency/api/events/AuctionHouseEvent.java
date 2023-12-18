package io.github.lightman314.lightmanscurrency.api.events;

import java.util.List;
import java.util.Objects;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;

public class AuctionHouseEvent extends Event {

	protected final AuctionHouseTrader auctionHouse;
	@Nonnull
	public AuctionHouseTrader getAuctionHouse() { return this.auctionHouse; }
	
	protected AuctionHouseEvent(@Nonnull AuctionHouseTrader auctionHouse) { this.auctionHouse = auctionHouse; }
	
	public static class AuctionEvent extends AuctionHouseEvent {
		
		protected AuctionTradeData auction;
		@Nonnull
		public AuctionTradeData getAuction() { return this.auction; }

		protected AuctionEvent(@Nonnull AuctionHouseTrader auctionHouse, @Nonnull AuctionTradeData auction) { super(auctionHouse); this.auction = auction; }
		
		public static class CreateAuctionEvent extends AuctionEvent {
			
			protected final boolean persistent;
			public boolean isPersistent() { return this.persistent; }
			
			protected CreateAuctionEvent(AuctionHouseTrader auctionHouse, AuctionTradeData auction, boolean persistent) {
				super(auctionHouse, auction);
				this.persistent = persistent;
			}
			
			public static final class Pre extends CreateAuctionEvent {

				public Pre(@Nonnull AuctionHouseTrader auctionHouse, @Nonnull AuctionTradeData auction, boolean persistent) { super(auctionHouse, auction, persistent); }
				
				public void setAuction(AuctionTradeData auction) {
					Objects.requireNonNull(auction);
					this.auction = auction;
				}
				
				@Override
				public boolean isCancelable() { return !this.isPersistent(); }
				
			}
			
			public static final class Post extends CreateAuctionEvent {
				public Post(@Nonnull AuctionHouseTrader auctionHouse, @Nonnull AuctionTradeData auction, boolean persistent) { super(auctionHouse, auction, persistent); }
			}
			
		}
		
		public static class CancelAuctionEvent extends AuctionEvent {

			protected final Player player;
			@Nonnull
			public Player getPlayer() { return this.player; }
			
			public CancelAuctionEvent(@Nonnull AuctionHouseTrader auctionHouse, @Nonnull AuctionTradeData auction, @Nonnull Player player) {
				super(auctionHouse, auction);
				this.player = player;
			}
			
		}
		
		public static class AuctionCompletedEvent extends AuctionEvent {

			public boolean hadBidder() { return this.auction.getLastBidPlayer() != null; }
			
			List<ItemStack> items;
			@Nonnull
			public List<ItemStack> getItems() { return this.items; }
			public void setItems(@Nonnull List<ItemStack> bidderRewards) { this.items = Objects.requireNonNull(bidderRewards); }

			MoneyValue paymentAmount;
			@Nonnull
			public MoneyValue getPayment() { return this.paymentAmount; }
			public void setPayment(@Nonnull MoneyValue paymentAmount) { this.paymentAmount = Objects.requireNonNull(paymentAmount); }
			
			public AuctionCompletedEvent(@Nonnull AuctionHouseTrader auctionHouse, @Nonnull AuctionTradeData auction) {
				super(auctionHouse, auction);
				this.items = this.auction.getAuctionItems();
				if(this.hadBidder())
					this.paymentAmount = this.auction.getLastBidAmount();
				else
					this.paymentAmount = MoneyValue.empty();
			}
			
		}
		
		public static class AuctionBidEvent extends AuctionEvent {

			protected final Player bidder;
			@Nonnull
			public Player getBidder() { return this.bidder; }
			
			protected MoneyValue bidAmount;
			@Nonnull
			public MoneyValue getBidAmount() { return this.bidAmount; }
			
			protected AuctionBidEvent(@Nonnull AuctionHouseTrader auctionHouse, @Nonnull AuctionTradeData auction, @Nonnull Player bidder, @Nonnull MoneyValue bidAmount) {
				super(auctionHouse, auction);
				this.bidder = bidder;
				this.bidAmount = bidAmount;
			}
			
			@Cancelable
			public static class Pre extends AuctionBidEvent {

				public void setBidAmount(@Nonnull MoneyValue bidAmount) { this.bidAmount = Objects.requireNonNull(bidAmount); }
				
				public Pre(@Nonnull AuctionHouseTrader auctionHouse, @Nonnull AuctionTradeData auction, @Nonnull Player bidder, @Nonnull MoneyValue bidAmount) {
					super(auctionHouse, auction, bidder, bidAmount);
				}
				
			}
			
			public static class Post extends AuctionBidEvent {
				
				public Post(@Nonnull AuctionHouseTrader auctionHouse, @Nonnull AuctionTradeData auction, @Nonnull Player bidder, @Nonnull MoneyValue bidAmount) {
					super(auctionHouse, auction, bidder, bidAmount);
				}
			}
			
		}
		
	}
	
}
