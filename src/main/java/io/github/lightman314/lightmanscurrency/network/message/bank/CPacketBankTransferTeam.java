package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.interfaces.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.bank.reference.types.TeamBankReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CPacketBankTransferTeam extends ClientToServerPacket {

	public static final Handler<CPacketBankTransferTeam> HANDLER = new H();

	long teamID;
	MoneyValue amount;
	
	public CPacketBankTransferTeam(long teamID, MoneyValue amount) {
		this.teamID = teamID;
		this.amount = amount;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeLong(this.teamID);
		this.amount.encode(buffer);
	}

	private static class H extends Handler<CPacketBankTransferTeam>
	{
		@Nonnull
		@Override
		public CPacketBankTransferTeam decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketBankTransferTeam(buffer.readLong(), MoneyValue.decode(buffer)); }
		@Override
		protected void handle(@Nonnull CPacketBankTransferTeam message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				if(sender.containerMenu instanceof IBankAccountAdvancedMenu menu)
				{
					BankReference destination = TeamBankReference.of(message.teamID);
					MutableComponent response = BankAccount.TransferCoins(menu, message.amount, destination);
					if(response != null)
						new SPacketBankTransferResponse(response).sendTo(sender);
				}
			}
		}
	}

}
