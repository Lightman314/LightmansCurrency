package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemEditCapable;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageOpenItemEdit implements IMessage<MessageOpenItemEdit> {
	
	int tradeIndex;
	
	public MessageOpenItemEdit()
	{
		
	}
	
	public MessageOpenItemEdit(int tradeIndex)
	{
		this.tradeIndex = tradeIndex;
	}
	
	@Override
	public void encode(MessageOpenItemEdit message, PacketBuffer buffer) {
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageOpenItemEdit decode(PacketBuffer buffer) {
		return new MessageOpenItemEdit(buffer.readInt());
	}

	@Override
	public void handle(MessageOpenItemEdit message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof IItemEditCapable)
				{
					IItemEditCapable container = (IItemEditCapable)entity.openContainer;
					container.openItemEditScreenForTrade(message.tradeIndex);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
