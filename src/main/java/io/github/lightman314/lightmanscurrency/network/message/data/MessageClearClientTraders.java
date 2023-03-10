package io.github.lightman314.lightmanscurrency.network.message.data;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageClearClientTraders {

	public static void handle(MessageClearClientTraders message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(LightmansCurrency.PROXY::clearClientTraders);
		supplier.get().setPacketHandled(true);
	}

}
