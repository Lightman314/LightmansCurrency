package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageBankInteraction {
	
	boolean isDeposit;
	CoinValue amount;
	
	public MessageBankInteraction(boolean isDeposit, CoinValue amount)
	{
		this.isDeposit = isDeposit;
		this.amount = amount;
	}
	
	public static void encode(MessageBankInteraction message, PacketBuffer buffer) {
		buffer.writeBoolean(message.isDeposit);
		buffer.writeCompoundTag(message.amount.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
	}

	public static MessageBankInteraction decode(PacketBuffer buffer) {
		return new MessageBankInteraction(buffer.readBoolean(), new CoinValue(buffer.readCompoundTag()));
	}

	public static void handle(MessageBankInteraction message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof IBankAccountMenu)
				{
					IBankAccountMenu container = (IBankAccountMenu) entity.openContainer;
					if(message.isDeposit)
						BankAccount.DepositCoins(container, message.amount);
					else
						BankAccount.WithdrawCoins(container, message.amount);
					container.onDepositOrWithdraw();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
