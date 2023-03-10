package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageCollectCoins {
	
	public static void encode(MessageCollectCoins message, PacketBuffer buffer) { }

	public static MessageCollectCoins decode(PacketBuffer buffer) {
		return new MessageCollectCoins();
	}

	public static void handle(MessageCollectCoins message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
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