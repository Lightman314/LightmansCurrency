package io.github.lightman314.lightmanscurrency.network.message.paygate;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.PaygateContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
//import io.github.lightman314.lightmanscurrency.containers.PaygateContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageActivatePaygate implements IMessage<MessageActivatePaygate> {
	
	public MessageActivatePaygate()
	{
		
	}
	
	
	@Override
	public void encode(MessageActivatePaygate message, FriendlyByteBuf buffer) {
	}

	@Override
	public MessageActivatePaygate decode(FriendlyByteBuf buffer) {
		return new MessageActivatePaygate();
	}

	@Override
	public void handle(MessageActivatePaygate message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof PaygateContainer)
				{
					PaygateContainer container = (PaygateContainer)entity.containerMenu;
					container.Activate();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
