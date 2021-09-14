package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
//import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageItemEditClose implements IMessage<MessageItemEditClose> {
	
	
	public MessageItemEditClose()
	{
		
	}
	
	@Override
	public void encode(MessageItemEditClose message, FriendlyByteBuf buffer) {
		
	}

	@Override
	public MessageItemEditClose decode(FriendlyByteBuf buffer) {
		return new MessageItemEditClose();
	}

	@Override
	public void handle(MessageItemEditClose message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ItemEditContainer)
				{
					ItemEditContainer container = (ItemEditContainer)entity.containerMenu;
					container.openTraderStorage();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
