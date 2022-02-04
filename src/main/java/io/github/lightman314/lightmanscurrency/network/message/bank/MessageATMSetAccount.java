package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.menus.ATMMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageATMSetAccount {
	
	private AccountReference account;
	
	public MessageATMSetAccount(AccountReference account)
	{
		this.account = account;
	}
	
	
	public static void encode(MessageATMSetAccount message, FriendlyByteBuf buffer) {
		message.account.writeToBuffer(buffer);
	}

	public static MessageATMSetAccount decode(FriendlyByteBuf buffer) {
		return new MessageATMSetAccount(BankAccount.LoadReference(false, buffer));
	}

	public static void handle(MessageATMSetAccount message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ATMMenu)
				{
					ATMMenu menu = (ATMMenu) player.containerMenu;
					menu.SetAccount(message.account);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
