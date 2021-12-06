package io.github.lightman314.lightmanscurrency.network.message.cashregister;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderCashRegisterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageCRSkipTo {
	
	int index;
	
	public MessageCRSkipTo(int index)
	{
		this.index = index;
	}
	
	public static void encode(MessageCRSkipTo message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.index);
	}

	public static MessageCRSkipTo decode(FriendlyByteBuf buffer) {
		return new MessageCRSkipTo(buffer.readInt());
	}

	public static void handle(MessageCRSkipTo message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ITraderCashRegisterMenu)
				{
					ITraderCashRegisterMenu menu = (ITraderCashRegisterMenu) player.containerMenu;
					menu.OpenContainerIndex(message.index);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
