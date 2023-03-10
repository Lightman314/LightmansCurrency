package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageSelectBankAccount {
	
	final AccountReference account;
	
	public MessageSelectBankAccount(AccountReference account) { this.account = account; }
	
	public static void encode(MessageSelectBankAccount message, PacketBuffer buffer) {
		message.account.writeToBuffer(buffer);
	}

	public static MessageSelectBankAccount decode(PacketBuffer buffer) {
		return new MessageSelectBankAccount(BankAccount.LoadReference(false, buffer));
	}

	public static void handle(MessageSelectBankAccount message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
				BankSaveData.SetSelectedBankAccount(player, message.account);
		});
		supplier.get().setPacketHandled(true);
	}

}