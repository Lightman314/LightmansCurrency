package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.interfaces.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CPacketBankInteraction extends ClientToServerPacket {

	public static final Handler<CPacketBankInteraction> HANDLER = new H();

	boolean isDeposit;
	CoinValue amount;
	
	public CPacketBankInteraction(boolean isDeposit, CoinValue amount) {
		this.isDeposit = isDeposit;
		this.amount = amount;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeBoolean(this.isDeposit);
		this.amount.encode(buffer);
	}

	private static final class H extends Handler<CPacketBankInteraction>
	{
		@Nonnull
		@Override
		public CPacketBankInteraction decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketBankInteraction(buffer.readBoolean(), CoinValue.decode(buffer)); }
		@Override
		protected void handle(@Nonnull CPacketBankInteraction message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				if(sender.containerMenu instanceof IBankAccountMenu menu)
				{
					if(message.isDeposit)
						BankAccount.DepositCoins(menu, message.amount);
					else
						BankAccount.WithdrawCoins(menu, message.amount);
					menu.onDepositOrWithdraw();
				}
			}
		}
	}

}
