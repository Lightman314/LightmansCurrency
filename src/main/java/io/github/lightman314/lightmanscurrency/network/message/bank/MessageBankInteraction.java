package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageBankInteraction {
	
	boolean isDeposit;
	CoinValue amount;
	
	public MessageBankInteraction(boolean isDeposit, CoinValue amount) {
		this.isDeposit = isDeposit;
		this.amount = amount;
	}
	
	public static void encode(MessageBankInteraction message, PacketBuffer buffer) {
		buffer.writeBoolean(message.isDeposit);
		message.amount.encode(buffer);
	}

	public static MessageBankInteraction decode(PacketBuffer buffer) {
		return new MessageBankInteraction(buffer.readBoolean(), CoinValue.decode(buffer));
	}

	public static void handle(MessageBankInteraction message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof IBankAccountMenu)
				{
					IBankAccountMenu menu = (IBankAccountMenu) player.containerMenu;
					if(message.isDeposit)
						BankAccount.DepositCoins(menu, message.amount);
					else
						BankAccount.WithdrawCoins(menu, message.amount);
					menu.onDepositOrWithdraw();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}