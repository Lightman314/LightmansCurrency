package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageSyncTrades implements IMessage<MessageSyncTrades> {
	
	
	public MessageSyncTrades()
	{
		
	}
	
	
	@Override
	public void encode(MessageSyncTrades message, FriendlyByteBuf buffer) {
		//buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageSyncTrades decode(FriendlyByteBuf buffer) {
		return new MessageSyncTrades();
	}

	@Override
	public void handle(MessageSyncTrades message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu != null)
				{
					if(entity.containerMenu instanceof ITraderStorageContainer)
					{
						ITraderStorageContainer container = (ITraderStorageContainer)entity.containerMenu;
						container.SyncTrades();
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
