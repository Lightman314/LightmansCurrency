package io.github.lightman314.lightmanscurrency.network.message.ticket_machine;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.TicketMachineContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageCraftTicket {

	private boolean fullStack;
	
	public MessageCraftTicket(boolean fullStack)
	{
		this.fullStack = fullStack;
	}
	
	public static void encode(MessageCraftTicket message, FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.fullStack);
	}

	public static MessageCraftTicket decode(FriendlyByteBuf buffer) {
		return new MessageCraftTicket(buffer.readBoolean());
	}

	public static void handle(MessageCraftTicket message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof TicketMachineContainer)
				{
					TicketMachineContainer menu = (TicketMachineContainer) player.containerMenu;
					menu.craftTickets(message.fullStack);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
