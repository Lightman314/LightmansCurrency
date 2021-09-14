package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSyncTrades implements IMessage<MessageSyncTrades> {
	
	
	public MessageSyncTrades()
	{
		
	}
	
	
	@Override
	public void encode(MessageSyncTrades message, PacketBuffer buffer) {
		//buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageSyncTrades decode(PacketBuffer buffer) {
		return new MessageSyncTrades();
	}

	@Override
	public void handle(MessageSyncTrades message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer != null)
				{
					if(entity.openContainer instanceof ITraderStorageContainer)
					{
						ITraderStorageContainer container = (ITraderStorageContainer)entity.openContainer;
						container.SyncTrades();
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
