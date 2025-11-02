package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketBankTransferPlayer extends ClientToServerPacket {

	public static final Handler<CPacketBankTransferPlayer> HANDLER = new H();

	String playerName;
	MoneyValue amount;
	
	public CPacketBankTransferPlayer(String playerName, MoneyValue amount) {
		this.playerName = playerName;
		this.amount = amount;
	}
	
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeUtf(this.playerName);
		this.amount.encode(buffer);
	}

	private static class H extends Handler<CPacketBankTransferPlayer>
	{
		
		@Override
		public CPacketBankTransferPlayer decode(FriendlyByteBuf buffer) { return new CPacketBankTransferPlayer(buffer.readUtf(), MoneyValue.decode(buffer)); }
		@Override
		protected void handle(CPacketBankTransferPlayer message, Player player) {
			if(player.containerMenu instanceof IBankAccountAdvancedMenu menu)
			{
				BankReference destination = PlayerBankReference.of(PlayerReference.of(false, message.playerName));
				MutableComponent response = BankAPI.getApi().BankTransfer(menu, message.amount, destination.get());
				if(response != null)
					new SPacketBankTransferResponse(response).sendTo(player);
			}
		}
	}

}
