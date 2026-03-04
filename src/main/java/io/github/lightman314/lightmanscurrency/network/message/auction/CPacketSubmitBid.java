package io.github.lightman314.lightmanscurrency.network.message.auction;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketSubmitBid extends ClientToServerPacket {

	private static final Type<CPacketSubmitBid> TYPE = cType("auction_submit_bid");
    private static final StreamCodec<RegistryFriendlyByteBuf,CPacketSubmitBid> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,p -> p.auctionHouseID,
            ByteBufCodecs.INT,p -> p.tradeIndex,
            MoneyValue.STREAM_CODEC,p -> p.bidAmount,
            CPacketSubmitBid::new);
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

	private static class H extends Handler<CPacketSubmitBid>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(CPacketSubmitBid message, IPayloadContext context, Player player) {
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
