package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountTransferMenu;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageBankTransfer {
	
	AccountReference destination;
	CoinValue amount;
	
	public MessageBankTransfer(AccountReference destination, CoinValue amount) {
		this.destination = destination;
		this.amount = amount;
	}
	
	public static void encode(MessageBankTransfer message, FriendlyByteBuf buffer) {
		message.destination.writeToBuffer(buffer);
		buffer.writeNbt(message.amount.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
	}

	public static MessageBankTransfer decode(FriendlyByteBuf buffer) {
		return new MessageBankTransfer(BankAccount.LoadReference(false, buffer), new CoinValue(buffer.readAnySizeNbt()));
	}

	public static void handle(MessageBankTransfer message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof IBankAccountTransferMenu)
				{
					IBankAccountTransferMenu menu = (IBankAccountTransferMenu) player.containerMenu;
					BankAccount.TransferCoins(menu, message.amount, message.destination);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
