package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.interfaces.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.bank.reference.types.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketBankTransferPlayer extends ClientToServerPacket {

	public static final Handler<CPacketBankTransferPlayer> HANDLER = new H();

	String playerName;
	MoneyValue amount;
	
	public CPacketBankTransferPlayer(String playerName, MoneyValue amount) {
		this.playerName = playerName;
		this.amount = amount;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeUtf(this.playerName);
		this.amount.encode(buffer);
	}

	private static class H extends Handler<CPacketBankTransferPlayer>
	{
		@Nonnull
		@Override
		public CPacketBankTransferPlayer decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketBankTransferPlayer(buffer.readUtf(), MoneyValue.decode(buffer)); }
		@Override
		protected void handle(@Nonnull CPacketBankTransferPlayer message, @Nullable ServerPlayer sender) {
			if(sender != null && sender.containerMenu instanceof IBankAccountAdvancedMenu menu)
			{
				BankReference destination = PlayerBankReference.of(PlayerReference.of(false, message.playerName));
				MutableComponent response = BankAccount.TransferCoins(menu, message.amount, destination);
				if(response != null)
					new SPacketBankTransferResponse(response).sendTo(sender);
			}
		}
	}

}
