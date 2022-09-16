package io.github.lightman314.lightmanscurrency.network.message.data;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageRemoveClientTrader {
	
	long traderID;
	
	public MessageRemoveClientTrader(long traderID)
	{
		this.traderID = traderID;
	}
	
	public static void encode(MessageRemoveClientTrader message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.traderID);
	}

	public static MessageRemoveClientTrader decode(FriendlyByteBuf buffer) {
		return new MessageRemoveClientTrader(buffer.readLong());
	}

	public static void handle(MessageRemoveClientTrader message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.removeTrader(message.traderID));
		supplier.get().setPacketHandled(true);
	}

}
