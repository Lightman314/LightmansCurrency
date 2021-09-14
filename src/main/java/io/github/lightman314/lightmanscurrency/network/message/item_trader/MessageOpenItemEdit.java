package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemEditCapable;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
	public void encode(MessageOpenItemEdit message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageOpenItemEdit decode(FriendlyByteBuf buffer) {
		return new MessageOpenItemEdit(buffer.readInt());
	}

	@Override
	public void handle(MessageOpenItemEdit message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof IItemEditCapable)
				{
					IItemEditCapable container = (IItemEditCapable)entity.containerMenu;
					container.openItemEditScreenForTrade(message.tradeIndex);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
