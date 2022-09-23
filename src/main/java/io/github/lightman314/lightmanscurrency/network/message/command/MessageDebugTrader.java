package io.github.lightman314.lightmanscurrency.network.message.command;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageDebugTrader {
	
	final long traderID;
	
	public MessageDebugTrader(long traderID) { this.traderID = traderID; }
	
	public static void encode(MessageDebugTrader message, FriendlyByteBuf buffer) { buffer.writeLong(message.traderID); }
	
	public static MessageDebugTrader decode(FriendlyByteBuf buffer) { return new MessageDebugTrader(buffer.readLong()); }
	
	public static void handle(MessageDebugTrader message, Supplier<Context> supplier)
	{
		supplier.get().enqueueWork(() -> {
			TraderData trader = ClientTraderData.GetTrader(message.traderID);
			if(trader == null)
				LightmansCurrency.LogInfo("Client is missing trader with id " + message.traderID + "!");
			else
				LightmansCurrency.LogInfo("Client Trader NBT for trader " + message.traderID + ":\n" + trader.save());
		});
		supplier.get().setPacketHandled(true);
	}
	
}
