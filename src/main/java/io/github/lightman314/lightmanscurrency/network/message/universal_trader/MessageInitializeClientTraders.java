package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageInitializeClientTraders implements IMessage<MessageInitializeClientTraders> {
	
	CompoundNBT compound;
	
	public MessageInitializeClientTraders()
	{
		
	}
	
	public MessageInitializeClientTraders(CompoundNBT compound)
	{
		this.compound = compound;
	}
	
	@Override
	public void encode(MessageInitializeClientTraders message, PacketBuffer buffer) {
		buffer.writeCompoundTag(message.compound);
	}

	@Override
	public MessageInitializeClientTraders decode(PacketBuffer buffer) {
		return new MessageInitializeClientTraders(buffer.readCompoundTag());
	}

	@Override
	public void handle(MessageInitializeClientTraders message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.initializeTraders(message.compound));
		supplier.get().setPacketHandled(true);
	}

}
