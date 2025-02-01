package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CPacketSelectBankAccount extends ClientToServerPacket {

	public static final Handler<CPacketSelectBankAccount> HANDLER = new H();

	final BankReference account;
	
	public CPacketSelectBankAccount(BankReference account) { this.account = account; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { this.account.encode(buffer); }
	private static class H extends Handler<CPacketSelectBankAccount>
	{
		@Nonnull
		@Override
		public CPacketSelectBankAccount decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketSelectBankAccount(BankReference.decode(buffer)); }
		@Override
		protected void handle(@Nonnull CPacketSelectBankAccount message, @Nullable ServerPlayer sender) {
			BankDataCache data = BankDataCache.TYPE.get(false);
			if(data == null || sender == null)
				return;
			data.setSelectedAccount(sender,message.account);
		}
	}

}
