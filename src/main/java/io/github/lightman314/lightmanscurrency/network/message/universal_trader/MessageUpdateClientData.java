package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageUpdateClientData implements IMessage<MessageUpdateClientData> {
	
	CompoundNBT traderData;
	
	public MessageUpdateClientData()
	{
		
	}
	
	public MessageUpdateClientData(CompoundNBT traderData)
	{
		this.traderData = traderData;
	}
	
	@Override
	public void encode(MessageUpdateClientData message, PacketBuffer buffer) {
		buffer.writeCompoundTag(message.traderData);
	}

	@Override
	public MessageUpdateClientData decode(PacketBuffer buffer) {
		return new MessageUpdateClientData(buffer.readCompoundTag());
	}

	@Override
	public void handle(MessageUpdateClientData message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateTrader(message.traderData));
		supplier.get().setPacketHandled(true);
	}

}
