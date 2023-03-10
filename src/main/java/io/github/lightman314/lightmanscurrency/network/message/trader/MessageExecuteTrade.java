package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageExecuteTrade {

	private final int trader;
	private final int tradeIndex;
	
	public MessageExecuteTrade(int trader, int tradeIndex)
	{
		this.trader = trader;
		this.tradeIndex = tradeIndex;
	}
	
	public static void encode(MessageExecuteTrade message, PacketBuffer buffer) {
		buffer.writeInt(message.trader);
		buffer.writeInt(message.tradeIndex);
	}

	public static MessageExecuteTrade decode(PacketBuffer buffer) {
		return new MessageExecuteTrade(buffer.readInt(), buffer.readInt());
	}

	public static void handle(MessageExecuteTrade message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof TraderMenu)
				{
					TraderMenu menu = (TraderMenu)player.containerMenu;
					menu.ExecuteTrade(message.trader, message.tradeIndex);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}