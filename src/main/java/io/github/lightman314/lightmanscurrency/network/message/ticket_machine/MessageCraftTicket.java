package io.github.lightman314.lightmanscurrency.network.message.ticket_machine;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TicketMachineMenu;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageCraftTicket {

	private boolean fullStack;
	
	public MessageCraftTicket(boolean fullStack)
	{
		this.fullStack = fullStack;
	}
	
	public static void encode(MessageCraftTicket message, PacketBuffer buffer) {
		buffer.writeBoolean(message.fullStack);
	}

	public static MessageCraftTicket decode(PacketBuffer buffer) {
		return new MessageCraftTicket(buffer.readBoolean());
	}

	public static void handle(MessageCraftTicket message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof TicketMachineMenu)
				{
					TicketMachineMenu menu = (TicketMachineMenu) player.containerMenu;
					menu.craftTickets(message.fullStack);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
