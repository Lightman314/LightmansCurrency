package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageItemEditClose implements IMessage<MessageItemEditClose> {
	
	
	public MessageItemEditClose()
	{
		
	}
	
	@Override
	public void encode(MessageItemEditClose message, PacketBuffer buffer) {
		
	}

	@Override
	public MessageItemEditClose decode(PacketBuffer buffer) {
		return new MessageItemEditClose();
	}

	@Override
	public void handle(MessageItemEditClose message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ItemEditContainer)
				{
					ItemEditContainer container = (ItemEditContainer)entity.openContainer;
					container.openTraderStorage();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
