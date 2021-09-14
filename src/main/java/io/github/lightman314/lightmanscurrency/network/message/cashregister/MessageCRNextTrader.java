package io.github.lightman314.lightmanscurrency.network.message.cashregister;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderCashRegisterContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
	public void encode(MessageCRNextTrader message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.direction);
	}

	@Override
	public MessageCRNextTrader decode(FriendlyByteBuf buffer) {
		return new MessageCRNextTrader(buffer.readInt());
	}

	@Override
	public void handle(MessageCRNextTrader message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ITraderCashRegisterContainer)
				{
					ITraderCashRegisterContainer container = (ITraderCashRegisterContainer) entity.containerMenu;
					container.OpenNextContainer(message.direction);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
