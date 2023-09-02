package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSelectBankAccount {
	
	final BankReference account;
	
	public MessageSelectBankAccount(BankReference account) { this.account = account; }
	
	public static void encode(MessageSelectBankAccount message, FriendlyByteBuf buffer) {
		message.account.encode(buffer);
	}

	public static MessageSelectBankAccount decode(FriendlyByteBuf buffer) {
		return new MessageSelectBankAccount(BankReference.decode(buffer));
	}

	public static void handle(MessageSelectBankAccount message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
				BankSaveData.SetSelectedBankAccount(player, message.account);
		});
		supplier.get().setPacketHandled(true);
	}

}
