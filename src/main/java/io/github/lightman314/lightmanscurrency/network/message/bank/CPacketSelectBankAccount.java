package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketSelectBankAccount extends ClientToServerPacket {

	public static final Handler<CPacketSelectBankAccount> HANDLER = new H();

	final BankReference account;
	
	public CPacketSelectBankAccount(BankReference account) { this.account = account; }
	
	public void encode(FriendlyByteBuf buffer) { this.account.encode(buffer); }
	private static class H extends Handler<CPacketSelectBankAccount>
	{
		@Override
		public CPacketSelectBankAccount decode(FriendlyByteBuf buffer) { return new CPacketSelectBankAccount(BankReference.decode(buffer)); }
		@Override
		protected void handle(CPacketSelectBankAccount message, Player player) {
			BankDataCache data = BankDataCache.TYPE.get(false);
			if(data == null || player == null)
				return;
			data.setSelectedAccount(player,message.account);
		}
	}

}
