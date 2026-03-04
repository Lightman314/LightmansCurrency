package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketBankInteraction extends ClientToServerPacket {

	private static final Type<CPacketBankInteraction> TYPE = cType("bank_interaction");
    private static final StreamCodec<RegistryFriendlyByteBuf,CPacketBankInteraction> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,p -> p.isDeposit,
            MoneyValue.STREAM_CODEC,p -> p.amount,
            CPacketBankInteraction::new);
	public static final Handler<CPacketBankInteraction> HANDLER = new H();

	boolean isDeposit;
	MoneyValue amount;
	
	public CPacketBankInteraction(boolean isDeposit, MoneyValue amount) {
		super(TYPE);
		this.isDeposit = isDeposit;
		this.amount = amount;
	}

	private static final class H extends Handler<CPacketBankInteraction>
	{
		private H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(CPacketBankInteraction message, IPayloadContext context, Player player) {
			if(player.containerMenu instanceof IBankAccountMenu menu)
			{
				if(message.isDeposit)
					BankAPI.getApi().BankDeposit(menu,message.amount);
				else
					BankAPI.getApi().BankWithdraw(menu,message.amount);
				menu.onDepositOrWithdraw();
			}
		}
	}

}
