package io.github.lightman314.lightmanscurrency.network.message.data;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageUpdateClientTrader {
	
	CompoundNBT traderData;
	
	public MessageUpdateClientTrader(CompoundNBT traderData)
	{
		this.traderData = traderData;
	}
	
	public static void encode(MessageUpdateClientTrader message, PacketBuffer buffer) {
		buffer.writeNbt(message.traderData);
	}

	public static MessageUpdateClientTrader decode(PacketBuffer buffer) {
		return new MessageUpdateClientTrader(buffer.readAnySizeNbt());
	}

	public static void handle(MessageUpdateClientTrader message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateTrader(message.traderData));
		supplier.get().setPacketHandled(true);
	}

}
