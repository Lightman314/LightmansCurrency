package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketBankTransferPlayer extends ClientToServerPacket {

	private static final Type<CPacketBankTransferPlayer> TYPE = new Type<>(VersionUtil.lcResource("c_bank_transfer_player"));
	public static final Handler<CPacketBankTransferPlayer> HANDLER = new H();

	String playerName;
	MoneyValue amount;
	
	public CPacketBankTransferPlayer(String playerName, MoneyValue amount) {
		super(TYPE);
		this.playerName = playerName;
		this.amount = amount;
	}
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketBankTransferPlayer message) {
		buffer.writeUtf(message.playerName);
		message.amount.encode(buffer);
	}

	private static CPacketBankTransferPlayer decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketBankTransferPlayer(buffer.readUtf(), MoneyValue.decode(buffer)); }

	private static class H extends Handler<CPacketBankTransferPlayer>
	{
		protected H() { super(TYPE, easyCodec(CPacketBankTransferPlayer::encode,CPacketBankTransferPlayer::decode)); }
		@Override
		protected void handle(@Nonnull CPacketBankTransferPlayer message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof IBankAccountAdvancedMenu menu)
			{
				BankReference destination = PlayerBankReference.of(PlayerReference.of(false, message.playerName));
				MutableComponent response = BankAPI.API.BankTransfer(menu, message.amount, destination.get());
				if(response != null)
					context.reply(new SPacketBankTransferResponse(response));
			}
		}
	}

}
