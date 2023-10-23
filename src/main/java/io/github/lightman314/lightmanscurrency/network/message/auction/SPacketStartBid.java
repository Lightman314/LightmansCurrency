package io.github.lightman314.lightmanscurrency.network.message.auction;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.auction.AuctionBidTab;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketStartBid extends ServerToClientPacket {

	public static final Handler<SPacketStartBid> HANDLER = new H();

	final long auctionHouseID;
	final int tradeIndex;
	
	public SPacketStartBid(long auctionHouseID, int tradeIndex) {
		this.auctionHouseID = auctionHouseID;
		this.tradeIndex = tradeIndex;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeLong(this.auctionHouseID);
		buffer.writeInt(this.tradeIndex);
	}

	private static class H extends Handler<SPacketStartBid>
	{
		@Nonnull
		@Override
		public SPacketStartBid decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketStartBid(buffer.readLong(), buffer.readInt()); }
		@Override
		protected void handle(@Nonnull SPacketStartBid message, @Nullable ServerPlayer sender) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.screen instanceof TraderScreen screen)
				screen.setTab(new AuctionBidTab(screen, message.auctionHouseID, message.tradeIndex));
		}
	}
	
}
