package io.github.lightman314.lightmanscurrency.network.message.auction;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSubmitBid {

	final UUID auctionHouseID;
	final int tradeIndex;
	final CoinValue bidAmount;
	
	public MessageSubmitBid(UUID auctionHouseID, int tradeIndex, CoinValue bidAmount) {
		this.auctionHouseID = auctionHouseID;
		this.tradeIndex = tradeIndex;
		this.bidAmount = bidAmount;
	}
	
	public static void encode(MessageSubmitBid message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.auctionHouseID);
		buffer.writeInt(message.tradeIndex);
		buffer.writeNbt(message.bidAmount.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
	}
	
	public static MessageSubmitBid decode(FriendlyByteBuf buffer) {
		return new MessageSubmitBid(buffer.readUUID(), buffer.readInt(), new CoinValue(buffer.readAnySizeNbt()));
	}
	
	public static void handle(MessageSubmitBid message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Player player = supplier.get().getSender();
			if(player != null && player.containerMenu instanceof TraderMenu)
			{
				TraderMenu menu = (TraderMenu)player.containerMenu;
				//Get the auction house
				UniversalTraderData data = TradingOffice.getData(message.auctionHouseID);
				if(data instanceof AuctionHouseTrader)
				{
					AuctionHouseTrader ah = (AuctionHouseTrader)data;
					ah.makeBid(player, menu, message.tradeIndex, message.bidAmount);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}