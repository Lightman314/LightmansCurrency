package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketBankTransferAccount extends ClientToServerPacket {

	public static final Handler<CPacketBankTransferAccount> HANDLER = new H();

	BankReference target;
	MoneyValue amount;
	
	public CPacketBankTransferAccount(BankReference target, MoneyValue amount) {
		this.target = target;
		this.amount = amount;
	}
	
	public void encode(FriendlyByteBuf buffer) {
		this.target.encode(buffer);
		this.amount.encode(buffer);
	}

	private static class H extends Handler<CPacketBankTransferAccount>
	{
		
		@Override
		public CPacketBankTransferAccount decode(FriendlyByteBuf buffer) { return new CPacketBankTransferAccount(BankReference.decode(buffer), MoneyValue.decode(buffer)); }
		@Override
		protected void handle(CPacketBankTransferAccount message, Player player) {
            if(player.containerMenu instanceof IBankAccountAdvancedMenu menu)
            {
                MutableComponent response = BankAPI.getApi().BankTransfer(menu, message.amount, message.target.get());
                if(response != null)
                    new SPacketBankTransferResponse(response).sendTo(player);
            }
		}
	}

}
