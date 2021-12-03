package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
	public void encode(MessageExecuteTrade message, PacketBuffer buffer) {
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageExecuteTrade decode(PacketBuffer buffer) {
		return new MessageExecuteTrade(buffer.readInt());
	}

	@Override
	public void handle(MessageExecuteTrade message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ITraderContainer)
				{
					ITraderContainer container = (ITraderContainer) entity.openContainer;
					container.ExecuteTrade(message.tradeIndex);
				}
				else
					LightmansCurrency.LogWarning("Container being used by " + entity.getDisplayName().getString() + " is not an ITraderContainer.");
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
