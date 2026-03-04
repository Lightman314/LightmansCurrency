package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketSelectBankAccount extends ClientToServerPacket {

	private static final Type<CPacketSelectBankAccount> TYPE = cType("select_bank_account");
    private static final StreamCodec<RegistryFriendlyByteBuf,CPacketSelectBankAccount> STREAM_CODEC = BankReference.STREAM_CODEC
            .map(CPacketSelectBankAccount::new,p -> p.account);
	public static final Handler<CPacketSelectBankAccount> HANDLER = new H();

	final BankReference account;
	
	public CPacketSelectBankAccount(BankReference account) { super(TYPE); this.account = account; }

	private static class H extends Handler<CPacketSelectBankAccount>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(CPacketSelectBankAccount message, IPayloadContext context, Player player) {
			BankDataCache data = BankDataCache.TYPE.get(false);
			if(data == null)
				return;
			data.setSelectedAccount(player, message.account);
		}
	}

}
