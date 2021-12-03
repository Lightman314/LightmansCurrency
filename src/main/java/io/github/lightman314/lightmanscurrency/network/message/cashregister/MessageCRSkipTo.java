package io.github.lightman314.lightmanscurrency.network.message.cashregister;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderCashRegisterContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageCRSkipTo implements IMessage<MessageCRSkipTo> {
	
	int index;
	
	public MessageCRSkipTo()
	{
		
	}
	
	public MessageCRSkipTo(int index)
	{
		this.index = index;
	}
	
	@Override
	public void encode(MessageCRSkipTo message, PacketBuffer buffer) {
		buffer.writeInt(message.index);
	}

	@Override
	public MessageCRSkipTo decode(PacketBuffer buffer) {
		return new MessageCRSkipTo(buffer.readInt());
	}

	@Override
	public void handle(MessageCRSkipTo message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ITraderCashRegisterContainer)
				{
					ITraderCashRegisterContainer container = (ITraderCashRegisterContainer) entity.openContainer;
					container.OpenContainerIndex(message.index);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
