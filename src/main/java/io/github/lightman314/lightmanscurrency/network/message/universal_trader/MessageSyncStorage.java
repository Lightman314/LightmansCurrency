package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.IUniversalTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSyncStorage implements IMessage<MessageSyncStorage> {
	
	
	public MessageSyncStorage()
	{
		
	}
	
	
	@Override
	public void encode(MessageSyncStorage message, PacketBuffer buffer) {
		//buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageSyncStorage decode(PacketBuffer buffer) {
		return new MessageSyncStorage();
	}

	@Override
	public void handle(MessageSyncStorage message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer != null)
				{
					if(entity.openContainer instanceof IUniversalTraderStorageContainer)
					{
						IUniversalTraderStorageContainer container = (IUniversalTraderStorageContainer)entity.openContainer;
						container.CheckStorage();
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
