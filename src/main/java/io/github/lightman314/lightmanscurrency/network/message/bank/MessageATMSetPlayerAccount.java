package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageATMSetPlayerAccount {
	
	final String playerName;
	
	public MessageATMSetPlayerAccount(String playerName)
	{
		this.playerName = playerName;
	}
	
	
	public static void encode(MessageATMSetPlayerAccount message, PacketBuffer buffer) {
		buffer.writeUtf(message.playerName);
	}

	public static MessageATMSetPlayerAccount decode(PacketBuffer buffer) {
		return new MessageATMSetPlayerAccount(buffer.readUtf());
	}

	public static void handle(MessageATMSetPlayerAccount message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ATMMenu)
				{
					ATMMenu menu = (ATMMenu) player.containerMenu;
					IFormattableTextComponent response = menu.SetPlayerAccount(message.playerName);
					LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageATMPlayerAccountResponse(response));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
