package io.github.lightman314.lightmanscurrency.network.message.auction;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.auction.AuctionBidTab;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageStartBid {

	final long auctionHouseID;
	final int tradeIndex;
	
	public MessageStartBid(long auctionHouseID, int tradeIndex) {
		this.auctionHouseID = auctionHouseID;
		this.tradeIndex = tradeIndex;
	}
	
	public static void encode(MessageStartBid message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.auctionHouseID);
		buffer.writeInt(message.tradeIndex);
	}
	
	public static MessageStartBid decode(FriendlyByteBuf buffer) {
		return new MessageStartBid(buffer.readLong(), buffer.readInt());
	}
	
	public static void handle(MessageStartBid message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			if(mc != null&& mc.screen instanceof TraderScreen)
			{
				TraderScreen screen = (TraderScreen)mc.screen;
				screen.setTab(new AuctionBidTab(screen, message.auctionHouseID, message.tradeIndex));
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
