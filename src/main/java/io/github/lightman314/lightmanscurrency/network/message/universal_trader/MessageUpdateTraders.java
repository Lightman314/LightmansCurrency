package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageUpdateTraders implements IMessage<MessageUpdateTraders> {
	
	CompoundNBT compound;
	
	public MessageUpdateTraders()
	{
		
	}
	
	public MessageUpdateTraders(CompoundNBT compound)
	{
		this.compound = compound;
	}
	
	@Override
	public void encode(MessageUpdateTraders message, PacketBuffer buffer) {
		buffer.writeCompoundTag(message.compound);
	}

	@Override
	public MessageUpdateTraders decode(PacketBuffer buffer) {
		return new MessageUpdateTraders(buffer.readCompoundTag());
	}

	@Override
	public void handle(MessageUpdateTraders message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateTraders(message.compound));
		supplier.get().setPacketHandled(true);
	}

}
