package io.github.lightman314.lightmanscurrency.network.message.cashregister;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderCashRegisterContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
	public void encode(MessageCRSkipTo message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.index);
	}

	@Override
	public MessageCRSkipTo decode(FriendlyByteBuf buffer) {
		return new MessageCRSkipTo(buffer.readInt());
	}

	@Override
	public void handle(MessageCRSkipTo message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ITraderCashRegisterContainer)
				{
					ITraderCashRegisterContainer container = (ITraderCashRegisterContainer) entity.containerMenu;
					container.OpenContainerIndex(message.index);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
