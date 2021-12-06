package io.github.lightman314.lightmanscurrency.network.message.paygate;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.PaygateContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageActivatePaygate {
	
	public static void encode(MessageActivatePaygate message, FriendlyByteBuf buffer) { }

	public static MessageActivatePaygate decode(FriendlyByteBuf buffer) {
		return new MessageActivatePaygate();
	}

	public static void handle(MessageActivatePaygate message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof PaygateContainer)
				{
					PaygateContainer menu = (PaygateContainer)player.containerMenu;
					menu.Activate();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
