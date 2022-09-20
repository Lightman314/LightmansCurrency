package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageCollectCoins {
	
	public static void encode(MessageCollectCoins message, FriendlyByteBuf buffer) { }

	public static MessageCollectCoins decode(FriendlyByteBuf buffer) {
		return new MessageCollectCoins();
	}

	public static void handle(MessageCollectCoins message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof TraderMenu)
				{
					TraderMenu menu = (TraderMenu)player.containerMenu;
					menu.CollectCoinStorage();
				}
				else if(player.containerMenu instanceof TraderStorageMenu)
				{
					TraderStorageMenu menu = (TraderStorageMenu)player.containerMenu;
					menu.CollectCoinStorage();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}