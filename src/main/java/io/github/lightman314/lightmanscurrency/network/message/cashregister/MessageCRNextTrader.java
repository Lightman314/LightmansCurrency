package io.github.lightman314.lightmanscurrency.network.message.cashregister;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderCashRegisterContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageCRNextTrader {
	
	int direction;
	
	public MessageCRNextTrader(int direction)
	{
		this.direction = direction;
	}
	
	public static void encode(MessageCRNextTrader message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.direction);
	}

	public static MessageCRNextTrader decode(FriendlyByteBuf buffer) {
		return new MessageCRNextTrader(buffer.readInt());
	}

	public static void handle(MessageCRNextTrader message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ITraderCashRegisterContainer)
				{
					ITraderCashRegisterContainer menu = (ITraderCashRegisterContainer) player.containerMenu;
					menu.OpenNextContainer(message.direction);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
