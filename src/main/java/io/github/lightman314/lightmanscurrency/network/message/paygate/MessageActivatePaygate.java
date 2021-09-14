package io.github.lightman314.lightmanscurrency.network.message.paygate;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.containers.PaygateContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageActivatePaygate implements IMessage<MessageActivatePaygate> {
	
	public MessageActivatePaygate()
	{
		
	}
	
	
	@Override
	public void encode(MessageActivatePaygate message, PacketBuffer buffer) {
	}

	@Override
	public MessageActivatePaygate decode(PacketBuffer buffer) {
		return new MessageActivatePaygate();
	}

	@Override
	public void handle(MessageActivatePaygate message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof PaygateContainer)
				{
					PaygateContainer container = (PaygateContainer)entity.openContainer;
					container.Activate();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
