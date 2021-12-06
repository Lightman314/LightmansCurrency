package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageRemoveClientTrader {
	
	UUID traderId;
	
	public MessageRemoveClientTrader(UUID traderId)
	{
		this.traderId = traderId;
	}
	
	public static void encode(MessageRemoveClientTrader message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderId);
	}

	public static MessageRemoveClientTrader decode(FriendlyByteBuf buffer) {
		return new MessageRemoveClientTrader(buffer.readUUID());
	}

	public static void handle(MessageRemoveClientTrader message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.removeTrader(message.traderId));
		supplier.get().setPacketHandled(true);
	}

}
