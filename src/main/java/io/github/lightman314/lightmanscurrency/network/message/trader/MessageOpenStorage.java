package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageOpenStorage {
	
	private final long traderID;
	
	public MessageOpenStorage(long traderID)
	{
		this.traderID = traderID;
	}
	
	public static void encode(MessageOpenStorage message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.traderID);
	}

	public static MessageOpenStorage decode(FriendlyByteBuf buffer) {
		return new MessageOpenStorage(buffer.readLong());
	}

	public static void handle(MessageOpenStorage message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				TraderData trader = TraderSaveData.GetTrader(false, message.traderID);
				if(trader != null)
					trader.openStorageMenu(player);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}