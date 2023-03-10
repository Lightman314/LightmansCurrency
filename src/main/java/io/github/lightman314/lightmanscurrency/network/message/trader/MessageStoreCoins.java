package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageStoreCoins {
	
	public static void encode(MessageStoreCoins message, PacketBuffer buffer) { }

	public static MessageStoreCoins decode(PacketBuffer buffer) {
		return new MessageStoreCoins();
	}

	public static void handle(MessageStoreCoins message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof TraderStorageMenu)
				{
					TraderStorageMenu menu = (TraderStorageMenu) player.containerMenu;
					menu.AddCoins();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}