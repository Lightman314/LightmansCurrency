package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageItemEditClose {
	
	public static void encode(MessageItemEditClose message, FriendlyByteBuf buffer) { }

	public static MessageItemEditClose decode(FriendlyByteBuf buffer) {
		return new MessageItemEditClose();
	}

	public static void handle(MessageItemEditClose message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ItemEditContainer)
				{
					ItemEditContainer menu = (ItemEditContainer)player.containerMenu;
					menu.openTraderStorage();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
