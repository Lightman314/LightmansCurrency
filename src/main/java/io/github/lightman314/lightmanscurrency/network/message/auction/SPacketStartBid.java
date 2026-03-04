package io.github.lightman314.lightmanscurrency.network.message.auction;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.auction.AuctionBidTab;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketStartBid extends ServerToClientPacket {

	private static final Type<SPacketStartBid> TYPE = sType("auction_start_bid");
    private static final StreamCodec<RegistryFriendlyByteBuf,SPacketStartBid> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,p -> p.auctionHouseID,
            ByteBufCodecs.INT,p -> p.tradeIndex,
            SPacketStartBid::new);
	public static final Handler<SPacketStartBid> HANDLER = new H();

	final long auctionHouseID;
	final int tradeIndex;
	
	public SPacketStartBid(long auctionHouseID, int tradeIndex) {
		super(TYPE);
		this.auctionHouseID = auctionHouseID;
		this.tradeIndex = tradeIndex;
	}

	private static class H extends Handler<SPacketStartBid>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(SPacketStartBid message, IPayloadContext context, Player player) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.screen instanceof TraderScreen screen)
				screen.setTab(new AuctionBidTab(screen, message.auctionHouseID, message.tradeIndex));
		}
	}
	
}
