package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.ATMMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageATMConversion {
	
	private int buttonHit;
	
	public MessageATMConversion(int buttonHit)
	{
		this.buttonHit = buttonHit;
	}
	
	
	public static void encode(MessageATMConversion message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.buttonHit);
	}

	public static MessageATMConversion decode(FriendlyByteBuf buffer) {
		return new MessageATMConversion(buffer.readInt());
	}

	public static void handle(MessageATMConversion message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ATMMenu)
				{
					ATMMenu menu = (ATMMenu) player.containerMenu;
					menu.ConvertCoins(message.buttonHit);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
