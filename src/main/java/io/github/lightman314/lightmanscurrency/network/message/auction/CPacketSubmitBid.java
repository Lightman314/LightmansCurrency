package io.github.lightman314.lightmanscurrency.network.message.auction;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketSubmitBid extends ClientToServerPacket {

	public static final Handler<CPacketSubmitBid> HANDLER = new H();

	final long auctionHouseID;
	final int tradeIndex;
	final CoinValue bidAmount;
	
	public CPacketSubmitBid(long auctionHouseID, int tradeIndex, CoinValue bidAmount) {
		this.auctionHouseID = auctionHouseID;
		this.tradeIndex = tradeIndex;
		this.bidAmount = bidAmount;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeLong(this.auctionHouseID);
		buffer.writeInt(this.tradeIndex);
		this.bidAmount.encode(buffer);
	}

	private static class H extends Handler<CPacketSubmitBid>
	{
		@Nonnull
		@Override
		public CPacketSubmitBid decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketSubmitBid(buffer.readLong(), buffer.readInt(), CoinValue.decode(buffer)); }
		@Override
		protected void handle(@Nonnull CPacketSubmitBid message, @Nullable ServerPlayer sender) {
			if(sender != null && sender.containerMenu instanceof TraderMenu menu)
			{
				//Get the auction house
				TraderData data = TraderSaveData.GetTrader(false, message.auctionHouseID);
				if(data instanceof AuctionHouseTrader ah)
					ah.makeBid(sender, menu, message.tradeIndex, message.bidAmount);
			}
		}
	}
	
}
