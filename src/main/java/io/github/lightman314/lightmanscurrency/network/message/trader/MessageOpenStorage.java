package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageOpenStorage {
	
	private final long traderID;
	
	public MessageOpenStorage(long traderID)
	{
		this.traderID = traderID;
	}
	
	public static void encode(MessageOpenStorage message, PacketBuffer buffer) {
		buffer.writeLong(message.traderID);
	}

	public static MessageOpenStorage decode(PacketBuffer buffer) {
		return new MessageOpenStorage(buffer.readLong());
	}

	public static void handle(MessageOpenStorage message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
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