package io.github.lightman314.lightmanscurrency.network.message.cashregister;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderCashRegisterContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageCRNextTrader implements IMessage<MessageCRNextTrader> {
	
	int direction;
	
	public MessageCRNextTrader()
	{
		
	}
	
	public MessageCRNextTrader(int direction)
	{
		this.direction = direction;
	}
	
	@Override
	public void encode(MessageCRNextTrader message, PacketBuffer buffer) {
		buffer.writeInt(message.direction);
	}

	@Override
	public MessageCRNextTrader decode(PacketBuffer buffer) {
		return new MessageCRNextTrader(buffer.readInt());
	}

	@Override
	public void handle(MessageCRNextTrader message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ITraderCashRegisterContainer)
				{
					ITraderCashRegisterContainer container = (ITraderCashRegisterContainer) entity.openContainer;
					container.OpenNextContainer(message.direction);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
