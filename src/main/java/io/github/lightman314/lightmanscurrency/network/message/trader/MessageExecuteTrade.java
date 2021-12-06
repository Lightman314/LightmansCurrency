package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageExecuteTrade {

	private int tradeIndex;
	
	public MessageExecuteTrade(int tradeIndex)
	{
		this.tradeIndex = tradeIndex;
	}
	
	public static void encode(MessageExecuteTrade message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.tradeIndex);
	}

	public static MessageExecuteTrade decode(FriendlyByteBuf buffer) {
		return new MessageExecuteTrade(buffer.readInt());
	}

	public static void handle(MessageExecuteTrade message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ITraderMenu)
				{
					ITraderMenu menu = (ITraderMenu) player.containerMenu;
					menu.ExecuteTrade(message.tradeIndex);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
