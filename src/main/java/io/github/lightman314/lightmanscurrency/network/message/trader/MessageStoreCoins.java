package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageStoreCoins implements IMessage<MessageStoreCoins> {
	
	public MessageStoreCoins()
	{
		
	}
	
	
	@Override
	public void encode(MessageStoreCoins message, PacketBuffer buffer) {
		//buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageStoreCoins decode(PacketBuffer buffer) {
		return new MessageStoreCoins();
	}

	@Override
	public void handle(MessageStoreCoins message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ITraderStorageContainer)
				{
					ITraderStorageContainer container = (ITraderStorageContainer) entity.openContainer;
					container.AddCoins();
				}
				else
				{
					LightmansCurrency.LogWarning("MessageStoreCoins was sent from a client that does not have a trader storage container open.");
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
