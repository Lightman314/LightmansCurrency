package io.github.lightman314.lightmanscurrency.network.message.auction;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.auction.AuctionBidTab;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketStartBid extends ServerToClientPacket {

	private static final Type<SPacketStartBid> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_auction_start_bid"));
	public static final Handler<SPacketStartBid> HANDLER = new H();

	final long auctionHouseID;
	final int tradeIndex;
	
	public SPacketStartBid(long auctionHouseID, int tradeIndex) {
		super(TYPE);
		this.auctionHouseID = auctionHouseID;
		this.tradeIndex = tradeIndex;
	}

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketStartBid message) {
		buffer.writeLong(message.auctionHouseID);
		buffer.writeInt(message.tradeIndex);
	}
	private static SPacketStartBid decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketStartBid(buffer.readLong(),buffer.readInt()); }

	private static class H extends Handler<SPacketStartBid>
	{
		protected H() { super(TYPE, easyCodec(SPacketStartBid::encode,SPacketStartBid::decode)); }
		@Override
		protected void handle(@Nonnull SPacketStartBid message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.screen instanceof TraderScreen screen)
				screen.setTab(new AuctionBidTab(screen, message.auctionHouseID, message.tradeIndex));
		}
	}
	
}
