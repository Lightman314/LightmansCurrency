package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketSelectBankAccount extends ClientToServerPacket {

	private static final Type<CPacketSelectBankAccount> TYPE = new Type<>(VersionUtil.lcResource("c_select_bank_account"));
	public static final Handler<CPacketSelectBankAccount> HANDLER = new H();

	final BankReference account;
	
	public CPacketSelectBankAccount(BankReference account) { super(TYPE); this.account = account; }
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketSelectBankAccount message) { message.account.encode(buffer); }
	private static CPacketSelectBankAccount decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketSelectBankAccount(BankReference.decode(buffer)); }

	private static class H extends Handler<CPacketSelectBankAccount>
	{
		protected H() { super(TYPE, easyCodec(CPacketSelectBankAccount::encode,CPacketSelectBankAccount::decode)); }
		@Override
		protected void handle(@Nonnull CPacketSelectBankAccount message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			BankDataCache data = BankDataCache.TYPE.get(false);
			if(data == null)
				return;
			data.setSelectedAccount(player, message.account);
		}
	}

}
