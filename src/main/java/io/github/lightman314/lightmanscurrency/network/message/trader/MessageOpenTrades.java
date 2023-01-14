package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageOpenTrades {
	
	private final long traderID;
	
	public MessageOpenTrades(long traderID)
	{
		this.traderID = traderID;
	}
	
	public static void encode(MessageOpenTrades message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.traderID);
	}

	public static MessageOpenTrades decode(FriendlyByteBuf buffer) {
		return new MessageOpenTrades(buffer.readLong());
	}

	public static void handle(MessageOpenTrades message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				TraderData data = TraderSaveData.GetTrader(false, message.traderID);
				if(data != null)
					data.openTraderMenu(player);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}