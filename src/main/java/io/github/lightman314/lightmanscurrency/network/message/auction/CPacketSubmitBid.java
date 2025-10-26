package io.github.lightman314.lightmanscurrency.network.message.auction;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketSubmitBid extends ClientToServerPacket {

	private static final Type<CPacketSubmitBid> TYPE = new Type<>(VersionUtil.lcResource("c_auction_submit_bid"));
	public static final Handler<CPacketSubmitBid> HANDLER = new H();

	final long auctionHouseID;
	final int tradeIndex;
	final MoneyValue bidAmount;
	
	public CPacketSubmitBid(long auctionHouseID, int tradeIndex, MoneyValue bidAmount) {
		super(TYPE);
		this.auctionHouseID = auctionHouseID;
		this.tradeIndex = tradeIndex;
		this.bidAmount = bidAmount;
	}
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketSubmitBid message) {
		buffer.writeLong(message.auctionHouseID);
		buffer.writeInt(message.tradeIndex);
		message.bidAmount.encode(buffer);
	}
	private static CPacketSubmitBid decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketSubmitBid(buffer.readLong(), buffer.readInt(),MoneyValue.decode(buffer)); }

	private static class H extends Handler<CPacketSubmitBid>
	{
		protected H() { super(TYPE, easyCodec(CPacketSubmitBid::encode,CPacketSubmitBid::decode)); }
		@Override
		protected void handle(@Nonnull CPacketSubmitBid message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof TraderMenu menu)
			{
				//Get the auction house
				TraderData data = TraderAPI.getApi().GetTrader(false, message.auctionHouseID);
				if(data instanceof AuctionHouseTrader ah)
					ah.makeBid(player, menu, message.tradeIndex, message.bidAmount);
			}
		}
	}
	
}
