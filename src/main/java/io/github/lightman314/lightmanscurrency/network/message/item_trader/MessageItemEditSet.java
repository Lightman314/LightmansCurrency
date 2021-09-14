package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
//import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageItemEditSet implements IMessage<MessageItemEditSet> {
	
	private ItemStack item;
	
	public MessageItemEditSet()
	{
		
	}
	
	public MessageItemEditSet(ItemStack item)
	{
		this.item = item;
	}
	
	@Override
	public void encode(MessageItemEditSet message, FriendlyByteBuf buffer) {
		buffer.writeItem(message.item);
	}

	@Override
	public MessageItemEditSet decode(FriendlyByteBuf buffer) {
		return new MessageItemEditSet(buffer.readItem());
	}

	@Override
	public void handle(MessageItemEditSet message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ItemEditContainer)
				{
					ItemEditContainer container = (ItemEditContainer)entity.containerMenu;
					container.setItem(message.item);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
