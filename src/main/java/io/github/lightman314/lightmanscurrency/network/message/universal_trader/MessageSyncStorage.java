package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.interfaces.IUniversalTraderStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MessageSyncStorage {
	
	public static void encode(MessageSyncStorage message, FriendlyByteBuf buffer) { }

	public static MessageSyncStorage decode(FriendlyByteBuf buffer) {
		return new MessageSyncStorage();
	}

	public static void handle(MessageSyncStorage message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof IUniversalTraderStorageMenu)
				{
					IUniversalTraderStorageMenu menu = (IUniversalTraderStorageMenu)player.containerMenu;
					menu.CheckStorage();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
