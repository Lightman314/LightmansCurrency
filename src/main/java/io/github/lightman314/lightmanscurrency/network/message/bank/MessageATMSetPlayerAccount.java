package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageATMSetPlayerAccount {
	
	private String playerName;
	
	public MessageATMSetPlayerAccount(String playerName)
	{
		this.playerName = playerName;
	}
	
	
	public static void encode(MessageATMSetPlayerAccount message, FriendlyByteBuf buffer) {
		buffer.writeUtf(message.playerName);
	}

	public static MessageATMSetPlayerAccount decode(FriendlyByteBuf buffer) {
		return new MessageATMSetPlayerAccount(buffer.readUtf());
	}

	public static void handle(MessageATMSetPlayerAccount message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ATMMenu)
				{
					ATMMenu menu = (ATMMenu) player.containerMenu;
					Pair<AccountReference,MutableComponent> response = menu.SetPlayerAccount(message.playerName);
					LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageATMPlayerAccountResponse(response.getFirst(), response.getSecond()));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
