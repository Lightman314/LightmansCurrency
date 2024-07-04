package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketBankInteraction extends ClientToServerPacket {

	private static final Type<CPacketBankInteraction> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_bank_interaction"));
	public static final Handler<CPacketBankInteraction> HANDLER = new H();

	boolean isDeposit;
	MoneyValue amount;
	
	public CPacketBankInteraction(boolean isDeposit, MoneyValue amount) {
		super(TYPE);
		this.isDeposit = isDeposit;
		this.amount = amount;
	}
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketBankInteraction message) {
		buffer.writeBoolean(message.isDeposit);
		message.amount.encode(buffer);
	}
	private static CPacketBankInteraction decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketBankInteraction(buffer.readBoolean(), MoneyValue.decode(buffer)); }

	private static final class H extends Handler<CPacketBankInteraction>
	{
		private H() { super(TYPE, easyCodec(CPacketBankInteraction::encode,CPacketBankInteraction::decode)); }
		@Override
		protected void handle(@Nonnull CPacketBankInteraction message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof IBankAccountMenu menu)
			{
				if(message.isDeposit)
					BankAPI.API.BankDeposit(menu, message.amount);
				else
					BankAPI.API.BankWithdraw(menu, message.amount);
				menu.onDepositOrWithdraw();
			}
		}
	}

}
