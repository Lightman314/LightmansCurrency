package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ICreativeTraderContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageAddOrRemoveTrade implements IMessage<MessageAddOrRemoveTrade> {
	
	public boolean isTradeAdd;
	
	public MessageAddOrRemoveTrade()
	{
		
	}
	
	public MessageAddOrRemoveTrade(boolean isTradeAdd)
	{
		this.isTradeAdd = isTradeAdd;
	}
	
	
	@Override
	public void encode(MessageAddOrRemoveTrade message, PacketBuffer buffer) {
		buffer.writeBoolean(message.isTradeAdd);
	}

	@Override
	public MessageAddOrRemoveTrade decode(PacketBuffer buffer) {
		return new MessageAddOrRemoveTrade(buffer.readBoolean());
	}

	@Override
	public void handle(MessageAddOrRemoveTrade message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ICreativeTraderContainer)
				{
					ICreativeTraderContainer container = (ICreativeTraderContainer)entity.openContainer;
					if(message.isTradeAdd)
						container.AddTrade();
					else
						container.RemoveTrade();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
