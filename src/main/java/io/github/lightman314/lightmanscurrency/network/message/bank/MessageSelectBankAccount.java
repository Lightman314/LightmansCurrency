package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSelectBankAccount {
	
	final AccountReference account;
	
	public MessageSelectBankAccount(AccountReference account) { this.account = account; }
	
	public static void encode(MessageSelectBankAccount message, FriendlyByteBuf buffer) {
		message.account.writeToBuffer(buffer);
	}

	public static MessageSelectBankAccount decode(FriendlyByteBuf buffer) {
		return new MessageSelectBankAccount(BankAccount.LoadReference(false, buffer));
	}

	public static void handle(MessageSelectBankAccount message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
				TradingOffice.setSelectedBankAccount(player, message.account);
		});
		supplier.get().setPacketHandled(true);
	}

}
