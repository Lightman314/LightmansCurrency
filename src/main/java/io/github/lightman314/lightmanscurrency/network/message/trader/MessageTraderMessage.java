package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageTraderMessage {
	
	long traderID;
	CompoundNBT message;
	
	public MessageTraderMessage(long traderID, CompoundNBT message)
	{
		this.traderID = traderID;
		this.message = message;
	}
	
	public static void encode(MessageTraderMessage message, PacketBuffer buffer) {
		buffer.writeLong(message.traderID);
		buffer.writeNbt(message.message);
	}

	public static MessageTraderMessage decode(PacketBuffer buffer) {
		return new MessageTraderMessage(buffer.readLong(), buffer.readAnySizeNbt());
	}

	public static void handle(MessageTraderMessage message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
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