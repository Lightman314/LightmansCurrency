package io.github.lightman314.lightmanscurrency.network.message.atm;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.ATMMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MessageATM {
	
	private int buttonHit;
	
	public MessageATM(int buttonHit)
	{
		this.buttonHit = buttonHit;
	}
	
	
	public static void encode(MessageATM message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.buttonHit);
	}

	public static MessageATM decode(FriendlyByteBuf buffer) {
		return new MessageATM(buffer.readInt());
	}

	public static void handle(MessageATM message, Supplier<Context> supplier) {
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
