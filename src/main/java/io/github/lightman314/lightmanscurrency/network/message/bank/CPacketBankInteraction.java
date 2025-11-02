package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketBankInteraction extends ClientToServerPacket {

	public static final Handler<CPacketBankInteraction> HANDLER = new H();

	boolean isDeposit;
	MoneyValue amount;
	
	public CPacketBankInteraction(boolean isDeposit, MoneyValue amount) {
		this.isDeposit = isDeposit;
		this.amount = amount;
	}
	
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBoolean(this.isDeposit);
		this.amount.encode(buffer);
	}

	private static final class H extends Handler<CPacketBankInteraction>
	{
		
		@Override
		public CPacketBankInteraction decode(FriendlyByteBuf buffer) { return new CPacketBankInteraction(buffer.readBoolean(), MoneyValue.decode(buffer)); }
		@Override
		protected void handle(CPacketBankInteraction message, Player player) {
            if(player.containerMenu instanceof IBankAccountMenu menu)
            {
                if(message.isDeposit)
                    BankAPI.getApi().BankDeposit(menu, message.amount);
                else
                    BankAPI.getApi().BankWithdraw(menu, message.amount);
                menu.onDepositOrWithdraw();
            }
		}
	}

}
