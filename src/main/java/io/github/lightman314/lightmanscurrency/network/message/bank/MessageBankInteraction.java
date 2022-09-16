package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageBankInteraction {
	
	boolean isDeposit;
	CoinValue amount;
	
	public MessageBankInteraction(boolean isDeposit, CoinValue amount) {
		this.isDeposit = isDeposit;
		this.amount = amount;
	}
	
	public static void encode(MessageBankInteraction message, FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.isDeposit);
		buffer.writeNbt(message.amount.save(new CompoundTag(), CoinValue.DEFAULT_KEY));
	}

	public static MessageBankInteraction decode(FriendlyByteBuf buffer) {
		return new MessageBankInteraction(buffer.readBoolean(), new CoinValue(buffer.readAnySizeNbt()));
	}

	public static void handle(MessageBankInteraction message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
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
