package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderContainerPrimitive;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageCollectCoins implements IMessage<MessageCollectCoins> {
	
	public MessageCollectCoins()
	{
		
	}
	
	
	@Override
	public void encode(MessageCollectCoins message, PacketBuffer buffer) {
		//buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageCollectCoins decode(PacketBuffer buffer) {
		return new MessageCollectCoins();
	}

	@Override
	public void handle(MessageCollectCoins message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ITraderContainerPrimitive)
				{
					ITraderContainerPrimitive container = (ITraderContainerPrimitive) entity.openContainer;
					container.CollectCoinStorage();
				}
				else
				{
					LightmansCurrency.LogWarning("MessageCollectCoins was sent from a client that does not have a trader container open.");
					if(entity.openContainer != null)
						LightmansCurrency.LogWarning("OpenContainer: " + entity.openContainer.getClass().getName());
					else
						LightmansCurrency.LogWarning("OpenContainer: NULL");
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
