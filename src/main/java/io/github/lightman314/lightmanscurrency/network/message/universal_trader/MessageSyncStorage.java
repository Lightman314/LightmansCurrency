package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.IUniversalTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageSyncStorage implements IMessage<MessageSyncStorage> {
	
	
	public MessageSyncStorage()
	{
		
	}
	
	
	@Override
	public void encode(MessageSyncStorage message, FriendlyByteBuf buffer) {
		//buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageSyncStorage decode(FriendlyByteBuf buffer) {
		return new MessageSyncStorage();
	}

	@Override
	public void handle(MessageSyncStorage message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu != null)
				{
					if(entity.containerMenu instanceof IUniversalTraderStorageContainer)
					{
						IUniversalTraderStorageContainer container = (IUniversalTraderStorageContainer)entity.containerMenu;
						container.CheckStorage();
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
