package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageClearClientTraders {
	
	public MessageClearClientTraders() { }
	
	public static void encode(MessageClearClientTraders message, FriendlyByteBuf buffer) { }

	public static MessageClearClientTraders decode(FriendlyByteBuf buffer) {
		return new MessageClearClientTraders();
	}

	public static void handle(MessageClearClientTraders message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.clearClientTraders());
		supplier.get().setPacketHandled(true);
	}

}
