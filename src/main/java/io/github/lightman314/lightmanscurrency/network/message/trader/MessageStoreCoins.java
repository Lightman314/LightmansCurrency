package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageStoreCoins implements IMessage<MessageStoreCoins> {
	
	public MessageStoreCoins()
	{
		
	}
	
	
	@Override
	public void encode(MessageStoreCoins message, FriendlyByteBuf buffer) {
		//buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageStoreCoins decode(FriendlyByteBuf buffer) {
		return new MessageStoreCoins();
	}

	@Override
	public void handle(MessageStoreCoins message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ITraderStorageContainer)
				{
					ITraderStorageContainer container = (ITraderStorageContainer) entity.containerMenu;
					container.AddCoins();
				}
				else
				{
					LightmansCurrency.LogWarning("MessageStoreCoins was sent from a client that does not have a trader storage container open.");
					if(entity.containerMenu != null)
						LightmansCurrency.LogWarning("OpenContainer: " + entity.containerMenu.getClass().getName());
					else
						LightmansCurrency.LogWarning("OpenContainer: NULL");
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
