package io.github.lightman314.lightmanscurrency.api.events;

import java.util.List;
import java.util.Objects;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import javax.annotation.Nullable;

public class AuctionHouseEvent extends Event {

	protected final TraderData auctionHouse;
	public TraderData getAuctionHouse() { return this.auctionHouse; }

	protected AuctionHouseEvent(TraderData auctionHouse) { this.auctionHouse = auctionHouse; }

	public static class AuctionEvent extends AuctionHouseEvent {

		protected AuctionTradeData auction;

		public AuctionTradeData getAuction() { return this.auction; }

		protected AuctionEvent(TraderData auctionHouse,AuctionTradeData auction) { super(auctionHouse); this.auction = auction; }

        /**
         * Event called whenever a new {@link AuctionTradeData} is created and added to the auction house<br>
         * Contains both a {@link Pre} and {@link Post} event
         */
        public static abstract class CreateAuctionEvent extends AuctionEvent {

			protected final Player player;
			@Nullable
			public Player getPlayer() { return this.player; }

			protected final boolean persistent;
			public boolean isPersistent() { return this.persistent; }

			protected CreateAuctionEvent(TraderData auctionHouse, AuctionTradeData auction,@Nullable Player player,boolean persistent) {
				super(auctionHouse, auction);
				this.persistent = persistent;
				this.player = player;
			}

            /**
             * Event called whenever a new {@link AuctionTradeData} is created and added to the auction house<br>
             * Event is {@link ICancellableEvent Cancellable}
             */
			public static final class Pre extends CreateAuctionEvent implements ICancellableEvent {

				public Pre(TraderData auctionHouse, AuctionTradeData auction, @Nullable Player player, boolean persistent) { super(auctionHouse, auction,player,persistent); }

				public void setAuction(AuctionTradeData auction) {
					Objects.requireNonNull(auction);
					this.auction = auction;
				}

				@Override
				public void setCanceled(boolean canceled) {
					if(this.isPersistent())
						return;
					ICancellableEvent.super.setCanceled(canceled);
				}
			}

			public static final class Post extends CreateAuctionEvent {
				public Post(TraderData auctionHouse, AuctionTradeData auction, @Nullable Player player, boolean persistent) { super(auctionHouse, auction, player, persistent); }
			}

		}

		public static class CancelAuctionEvent extends AuctionEvent {

			protected final Player player;
			public Player getPlayer() { return this.player; }

			public CancelAuctionEvent(TraderData auctionHouse, AuctionTradeData auction, Player player) {
				super(auctionHouse, auction);
				this.player = player;
			}

		}

		public static class AuctionCompletedEvent extends AuctionEvent {

			public boolean hadBidder() { return this.auction.getLastBidPlayer() != null; }

			List<ItemStack> items;

			public List<ItemStack> getItems() { return this.items; }
			public void setItems(List<ItemStack> bidderRewards) { this.items = Objects.requireNonNull(bidderRewards); }

			MoneyValue highestBid;
			MoneyValue paymentAmount;
			MoneyValue feeAmount;

			public MoneyValue getHighestBid() { return this.highestBid; }

			public int getAuctionFeePercent() { return LCConfig.SERVER.auctionHouseFeePercentage.get(); }

			public MoneyValue getFeePayment() { return this.feeAmount; }
			public void setFeePayment(MoneyValue feePayment) { this.feeAmount = Objects.requireNonNull(feePayment); }

			public MoneyValue getPaymentAmount() { return this.paymentAmount; }
			public void setPaymentAmount(MoneyValue paymentAmount) { this.paymentAmount = Objects.requireNonNull(paymentAmount); }

			public AuctionCompletedEvent(TraderData auctionHouse, AuctionTradeData auction) {
				super(auctionHouse, auction);
				this.items = this.auction.getAuctionItems();
				if(this.hadBidder())
				{
					this.highestBid = this.auction.getLastBidAmount();
					this.feeAmount = this.highestBid.percentageOfValue(this.getAuctionFeePercent());
					this.paymentAmount = this.highestBid.subtractValue(this.feeAmount);
				}
				else
					this.highestBid = this.paymentAmount = this.feeAmount = MoneyValue.empty();
			}

		}

		public static class AuctionBidEvent extends AuctionEvent {

			protected final Player bidder;

			public Player getBidder() { return this.bidder; }

			protected MoneyValue bidAmount;

			public MoneyValue getBidAmount() { return this.bidAmount; }

			protected AuctionBidEvent(TraderData auctionHouse, AuctionTradeData auction, Player bidder, MoneyValue bidAmount) {
				super(auctionHouse, auction);
				this.bidder = bidder;
				this.bidAmount = bidAmount;
			}

			public static class Pre extends AuctionBidEvent implements ICancellableEvent{

				public void setBidAmount(MoneyValue bidAmount) { this.bidAmount = Objects.requireNonNull(bidAmount); }

				public Pre(TraderData auctionHouse, AuctionTradeData auction, Player bidder, MoneyValue bidAmount) {
					super(auctionHouse, auction, bidder, bidAmount);
				}

			}

			public static class Post extends AuctionBidEvent {

				public Post(TraderData auctionHouse, AuctionTradeData auction, Player bidder, MoneyValue bidAmount) {
					super(auctionHouse, auction, bidder, bidAmount);
				}
			}

		}

	}

}
