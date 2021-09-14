package io.github.lightman314.lightmanscurrency.network.message.ticket_machine;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.TicketMachineContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
	public void encode(MessageCraftTicket message, FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.fullStack);
	}

	@Override
	public MessageCraftTicket decode(FriendlyByteBuf buffer) {
		return new MessageCraftTicket(buffer.readBoolean());
	}

	@Override
	public void handle(MessageCraftTicket message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof TicketMachineContainer)
				{
					TicketMachineContainer container = (TicketMachineContainer) entity.containerMenu;
					container.craftTickets(message.fullStack);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
