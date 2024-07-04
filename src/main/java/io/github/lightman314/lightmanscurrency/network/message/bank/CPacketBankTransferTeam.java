package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketBankTransferTeam extends ClientToServerPacket {

	private static final Type<CPacketBankTransferTeam> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_bank_transfer_team"));
	public static final Handler<CPacketBankTransferTeam> HANDLER = new H();

	long teamID;
	MoneyValue amount;
	
	public CPacketBankTransferTeam(long teamID, MoneyValue amount) {
		super(TYPE);
		this.teamID = teamID;
		this.amount = amount;
	}
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketBankTransferTeam message) {
		buffer.writeLong(message.teamID);
		message.amount.encode(buffer);
	}

	private static CPacketBankTransferTeam decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketBankTransferTeam(buffer.readLong(),MoneyValue.decode(buffer)); }

	private static class H extends Handler<CPacketBankTransferTeam>
	{
		protected H() { super(TYPE, easyCodec(CPacketBankTransferTeam::encode,CPacketBankTransferTeam::decode)); }
		@Override
		protected void handle(@Nonnull CPacketBankTransferTeam message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof IBankAccountAdvancedMenu menu)
			{
				BankReference destination = TeamBankReference.of(message.teamID);
				MutableComponent response = BankAPI.API.BankTransfer(menu, message.amount, destination.get());
				if(response != null)
					context.reply(new SPacketBankTransferResponse(response));
			}
		}
	}

}
