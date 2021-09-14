package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageToggleCreative implements IMessage<MessageToggleCreative> {
	
	public MessageToggleCreative()
	{
		
	}
	
	
	@Override
	public void encode(MessageToggleCreative message, FriendlyByteBuf buffer) {
		//buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageToggleCreative decode(FriendlyByteBuf buffer) {
		return new MessageToggleCreative();
	}

	@Override
	public void handle(MessageToggleCreative message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ITraderStorageContainer)
				{
					ITraderStorageContainer container = (ITraderStorageContainer) entity.containerMenu;
					container.ToggleCreative();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
