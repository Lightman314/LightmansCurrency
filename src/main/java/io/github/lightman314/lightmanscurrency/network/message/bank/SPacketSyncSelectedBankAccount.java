package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketSyncSelectedBankAccount extends ServerToClientPacket {

	public static final Handler<SPacketSyncSelectedBankAccount> HANDLER = new H();

	final BankReference selectedAccount;
	
	public SPacketSyncSelectedBankAccount(BankReference selectedAccount) { this.selectedAccount = selectedAccount; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { this.selectedAccount.encode(buffer); }

	private static class H extends Handler<SPacketSyncSelectedBankAccount>
	{
		@Nonnull
		@Override
		public SPacketSyncSelectedBankAccount decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncSelectedBankAccount(BankReference.decode(buffer).flagAsClient()); }
		@Override
		protected void handle(@Nonnull SPacketSyncSelectedBankAccount message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.receiveSelectedBankAccount(message.selectedAccount);
		}
	}
	
}
