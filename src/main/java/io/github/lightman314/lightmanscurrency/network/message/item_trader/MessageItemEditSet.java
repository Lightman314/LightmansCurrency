package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageItemEditSet implements IMessage<MessageItemEditSet> {
	
	private ItemStack item;
	private int slot;
	
	public MessageItemEditSet()
	{
		
	}
	
	public MessageItemEditSet(ItemStack item, int slot)
	{
		this.item = item;
		this.slot = slot;
	}
	
	@Override
	public void encode(MessageItemEditSet message, PacketBuffer buffer) {
		buffer.writeCompoundTag(message.item.write(new CompoundNBT()));
		buffer.writeInt(message.slot);
	}

	@Override
	public MessageItemEditSet decode(PacketBuffer buffer) {
		return new MessageItemEditSet(ItemStack.read(buffer.readCompoundTag()), buffer.readInt());
	}

	@Override
	public void handle(MessageItemEditSet message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ItemEditContainer)
				{
					ItemEditContainer container = (ItemEditContainer)entity.openContainer;
					container.setItem(message.item, message.slot);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
