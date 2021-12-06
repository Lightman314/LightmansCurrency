package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageToggleCreative {
	
	public static void encode(MessageToggleCreative message, FriendlyByteBuf buffer) { }

	public static MessageToggleCreative decode(FriendlyByteBuf buffer) {
		return new MessageToggleCreative();
	}

	public static void handle(MessageToggleCreative message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ITraderStorageContainer)
				{
					ITraderStorageContainer menu = (ITraderStorageContainer) player.containerMenu;
					menu.ToggleCreative();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
