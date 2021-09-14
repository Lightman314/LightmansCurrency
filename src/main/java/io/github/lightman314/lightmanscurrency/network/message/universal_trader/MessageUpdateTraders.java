package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageUpdateTraders implements IMessage<MessageUpdateTraders> {
	
	CompoundTag compound;
	
	public MessageUpdateTraders()
	{
		
	}
	
	public MessageUpdateTraders(CompoundTag compound)
	{
		this.compound = compound;
	}
	
	@Override
	public void encode(MessageUpdateTraders message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.compound);
	}

	@Override
	public MessageUpdateTraders decode(FriendlyByteBuf buffer) {
		return new MessageUpdateTraders(buffer.readNbt());
	}

	@Override
	public void handle(MessageUpdateTraders message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateTraders(message.compound));
		supplier.get().setPacketHandled(true);
	}

}
