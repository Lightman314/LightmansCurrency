package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageExecuteTrade {

	private final int trader;
	private final int tradeIndex;
	
	public MessageExecuteTrade(int trader, int tradeIndex)
	{
		this.trader = trader;
		this.tradeIndex = tradeIndex;
	}
	
	public static void encode(MessageExecuteTrade message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.trader);
		buffer.writeInt(message.tradeIndex);
	}

	public static MessageExecuteTrade decode(FriendlyByteBuf buffer) {
		return new MessageExecuteTrade(buffer.readInt(), buffer.readInt());
	}

	public static void handle(MessageExecuteTrade message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof TraderMenu menu)
					menu.ExecuteTrade(message.trader, message.tradeIndex);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}