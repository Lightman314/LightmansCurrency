package io.github.lightman314.lightmanscurrency.network.message.ticket_machine;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.TicketMachineContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageCraftTicket implements IMessage<MessageCraftTicket> {

	private boolean fullStack;
	
	public MessageCraftTicket()
	{
		
	}
	
	public MessageCraftTicket(boolean fullStack)
	{
		this.fullStack = fullStack;
	}
	
	
	@Override
	public void encode(MessageCraftTicket message, PacketBuffer buffer) {
		buffer.writeBoolean(message.fullStack);
	}

	@Override
	public MessageCraftTicket decode(PacketBuffer buffer) {
		return new MessageCraftTicket(buffer.readBoolean());
	}

	@Override
	public void handle(MessageCraftTicket message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof TicketMachineContainer)
				{
					TicketMachineContainer container = (TicketMachineContainer) entity.openContainer;
					container.craftTickets(message.fullStack);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
