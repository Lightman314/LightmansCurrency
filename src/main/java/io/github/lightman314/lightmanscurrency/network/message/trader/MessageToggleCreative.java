package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageToggleCreative implements IMessage<MessageToggleCreative> {
	
	public MessageToggleCreative()
	{
		
	}
	
	
	@Override
	public void encode(MessageToggleCreative message, PacketBuffer buffer) {
		//buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageToggleCreative decode(PacketBuffer buffer) {
		return new MessageToggleCreative();
	}

	@Override
	public void handle(MessageToggleCreative message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ITraderStorageContainer)
				{
					ITraderStorageContainer container = (ITraderStorageContainer) entity.openContainer;
					container.ToggleCreative();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
