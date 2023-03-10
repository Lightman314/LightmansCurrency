package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SPacketSyncSelectedBankAccount {

	final AccountReference selectedAccount;
	
	public SPacketSyncSelectedBankAccount(AccountReference selectedAccount) { this.selectedAccount = selectedAccount; }
	
	public static void encode(SPacketSyncSelectedBankAccount message, PacketBuffer buffer) {
		message.selectedAccount.writeToBuffer(buffer);
	}
	
	public static SPacketSyncSelectedBankAccount decode(PacketBuffer buffer) {
		return new SPacketSyncSelectedBankAccount(BankAccount.LoadReference(true, buffer));
	}
	
	public static void handle(SPacketSyncSelectedBankAccount message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.receiveSelectedBankAccount(message.selectedAccount));
		supplier.get().setPacketHandled(true);
	}
	
}
