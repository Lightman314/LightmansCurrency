package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.SelectionTab;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageATMPlayerAccountResponse {
	
	private AccountReference account;
	private Component message;
	
	public MessageATMPlayerAccountResponse(AccountReference account, Component message)
	{
		this.account = account;
		this.message = message;
	}
	
	
	public static void encode(MessageATMPlayerAccountResponse message, FriendlyByteBuf buffer) {
		if(message.account != null)
		{
			buffer.writeBoolean(true);
			message.account.writeToBuffer(buffer);
		}
		else
			buffer.writeBoolean(false);
		buffer.writeUtf(Component.Serializer.toJson(message.message));
	}

	public static MessageATMPlayerAccountResponse decode(FriendlyByteBuf buffer) {
		AccountReference account = null;
		if(buffer.readBoolean())
			account = BankAccount.LoadReference(true, buffer);
		return new MessageATMPlayerAccountResponse(account, Component.Serializer.fromJson(buffer.readUtf()));
	}

	public static void handle(MessageATMPlayerAccountResponse message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft mc = Minecraft.getInstance();
			if(mc.screen instanceof ATMScreen)
			{
				ATMScreen screen = (ATMScreen)mc.screen;
				if(screen.currentTab() instanceof SelectionTab)
					((SelectionTab)screen.currentTab()).ReceiveSelectPlayerResponse(message.account, message.message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
