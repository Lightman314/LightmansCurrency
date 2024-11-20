package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CPacketBankTransferAccount extends ClientToServerPacket {

	public static final Handler<CPacketBankTransferAccount> HANDLER = new H();

	BankReference target;
	MoneyValue amount;
	
	public CPacketBankTransferAccount(BankReference target, MoneyValue amount) {
		this.target = target;
		this.amount = amount;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		this.target.encode(buffer);
		this.amount.encode(buffer);
	}

	private static class H extends Handler<CPacketBankTransferAccount>
	{
		@Nonnull
		@Override
		public CPacketBankTransferAccount decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketBankTransferAccount(BankReference.decode(buffer), MoneyValue.decode(buffer)); }
		@Override
		protected void handle(@Nonnull CPacketBankTransferAccount message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				if(sender.containerMenu instanceof IBankAccountAdvancedMenu menu)
				{
					MutableComponent response = BankAPI.API.BankTransfer(menu, message.amount, message.target.get());
					if(response != null)
						new SPacketBankTransferResponse(response).sendTo(sender);
				}
			}
		}
	}

}
