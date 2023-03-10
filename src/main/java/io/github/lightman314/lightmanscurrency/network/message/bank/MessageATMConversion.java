package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageATMConversion {
	
	private final String command;
	
	public MessageATMConversion(String command)
	{
		this.command = command;
	}
	
	public static void encode(MessageATMConversion message, PacketBuffer buffer) {
		buffer.writeUtf(message.command);
	}

	public static MessageATMConversion decode(PacketBuffer buffer) {
		return new MessageATMConversion(buffer.readUtf());
	}

	public static void handle(MessageATMConversion message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ATMMenu)
				{
					ATMMenu menu = (ATMMenu) player.containerMenu;
					menu.ConvertCoins(message.command);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
