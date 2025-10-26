package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketBankTransferAccount extends ClientToServerPacket {

	private static final Type<CPacketBankTransferAccount> TYPE = new Type<>(VersionUtil.lcResource("c_bank_transfer_team"));
	public static final Handler<CPacketBankTransferAccount> HANDLER = new H();

	BankReference target;
	MoneyValue amount;
	
	public CPacketBankTransferAccount(BankReference target, MoneyValue amount) {
		super(TYPE);
		this.target = target;
		this.amount = amount;
	}
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketBankTransferAccount message) {
		message.target.encode(buffer);
		message.amount.encode(buffer);
	}

	private static CPacketBankTransferAccount decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketBankTransferAccount(BankReference.decode(buffer),MoneyValue.decode(buffer)); }

	private static class H extends Handler<CPacketBankTransferAccount>
	{
		protected H() { super(TYPE, easyCodec(CPacketBankTransferAccount::encode, CPacketBankTransferAccount::decode)); }
		@Override
		protected void handle(@Nonnull CPacketBankTransferAccount message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof IBankAccountAdvancedMenu menu)
			{
				MutableComponent response = BankAPI.getApi().BankTransfer(menu, message.amount, message.target.get());
				if(response != null)
					context.reply(new SPacketBankTransferResponse(response));
			}
		}
	}

}
