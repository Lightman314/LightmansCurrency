package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSyncUsers {
	
	long traderID;
	int userCount;
	
	public MessageSyncUsers(long traderID, int userCount)
	{
		this.traderID = traderID;
		this.userCount = userCount;
	}
	
	public static void encode(MessageSyncUsers message, PacketBuffer buffer) {
		buffer.writeLong(message.traderID);
		buffer.writeInt(message.userCount);
	}

	public static MessageSyncUsers decode(PacketBuffer buffer) {
		return new MessageSyncUsers(buffer.readLong(), buffer.readInt());
	}

	public static void handle(MessageSyncUsers message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			TraderData trader = TraderSaveData.GetTrader(true, message.traderID);
			if(trader != null)
				trader.updateUserCount(message.userCount);
		});
		supplier.get().setPacketHandled(true);
	}

}