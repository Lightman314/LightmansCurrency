package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageExecuteTrade implements IMessage<MessageExecuteTrade> {

	private int tradeIndex;
	
	public MessageExecuteTrade()
	{
		
	}
	
	public MessageExecuteTrade(int tradeIndex)
	{
		this.tradeIndex = tradeIndex;
	}
	
	
	@Override
	public void encode(MessageExecuteTrade message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageExecuteTrade decode(FriendlyByteBuf buffer) {
		return new MessageExecuteTrade(buffer.readInt());
	}

	@Override
	public void handle(MessageExecuteTrade message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ITraderContainer)
				{
					ITraderContainer container = (ITraderContainer) entity.containerMenu;
					container.ExecuteTrade(message.tradeIndex);
				}
				else
					LightmansCurrency.LogWarning("Container being used by " + entity.getDisplayName().getString() + " is not an ITraderContainer.");
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
