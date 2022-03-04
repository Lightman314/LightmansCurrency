package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountTransferMenu;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageBankTransfer {
	
	AccountReference destination;
	CoinValue amount;
	
	public MessageBankTransfer(AccountReference destination, CoinValue amount) {
		this.destination = destination;
		this.amount = amount;
	}
	
	public static void encode(MessageBankTransfer message, PacketBuffer buffer) {
		message.destination.writeToBuffer(buffer);
		buffer.writeCompoundTag(message.amount.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
	}

	public static MessageBankTransfer decode(PacketBuffer buffer) {
		return new MessageBankTransfer(BankAccount.LoadReference(false, buffer), new CoinValue(buffer.readCompoundTag()));
	}

	public static void handle(MessageBankTransfer message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.openContainer instanceof IBankAccountTransferMenu)
				{
					IBankAccountTransferMenu menu = (IBankAccountTransferMenu) player.openContainer;
					BankAccount.TransferCoins(menu, message.amount, message.destination);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
