package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageTraderMessage {
	
	long traderID;
	CompoundTag message;
	
	public MessageTraderMessage(long traderID, CompoundTag message)
	{
		this.traderID = traderID;
		this.message = message;
	}
	
	public static void encode(MessageTraderMessage message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.traderID);
		buffer.writeNbt(message.message);
	}

	public static MessageTraderMessage decode(FriendlyByteBuf buffer) {
		return new MessageTraderMessage(buffer.readLong(), buffer.readAnySizeNbt());
	}

	public static void handle(MessageTraderMessage message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				TraderData trader = TraderSaveData.GetTrader(false, message.traderID);
				if(trader != null)
					trader.receiveNetworkMessage(player, message.message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}