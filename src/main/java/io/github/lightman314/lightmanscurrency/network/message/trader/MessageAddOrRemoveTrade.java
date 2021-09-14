package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ICreativeTraderContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
	public void encode(MessageAddOrRemoveTrade message, FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.isTradeAdd);
	}

	@Override
	public MessageAddOrRemoveTrade decode(FriendlyByteBuf buffer) {
		return new MessageAddOrRemoveTrade(buffer.readBoolean());
	}

	@Override
	public void handle(MessageAddOrRemoveTrade message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ICreativeTraderContainer)
				{
					ICreativeTraderContainer container = (ICreativeTraderContainer)entity.containerMenu;
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
