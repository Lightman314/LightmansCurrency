package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

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
				if(player.containerMenu instanceof ITraderStorageMenu)
				{
					ITraderStorageMenu menu = (ITraderStorageMenu) player.containerMenu;
					menu.ToggleCreative();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
