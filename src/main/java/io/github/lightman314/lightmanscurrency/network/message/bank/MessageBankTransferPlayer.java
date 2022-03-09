package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountTransferMenu;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageBankTransferPlayer {
	
	String playerName;
	CoinValue amount;
	
	public MessageBankTransferPlayer(String playerName, CoinValue amount) {
		this.playerName = playerName;
		this.amount = amount;
	}
	
	public static void encode(MessageBankTransferPlayer message, PacketBuffer buffer) {
		buffer.writeString(message.playerName);
		buffer.writeCompoundTag(message.amount.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
	}

	public static MessageBankTransferPlayer decode(PacketBuffer buffer) {
		return new MessageBankTransferPlayer(buffer.readString(100), new CoinValue(buffer.readCompoundTag()));
	}

	public static void handle(MessageBankTransferPlayer message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.openContainer instanceof IBankAccountTransferMenu)
				{
					IBankAccountTransferMenu menu = (IBankAccountTransferMenu) player.openContainer;
					AccountReference destination = BankAccount.GenerateReference(false, PlayerReference.of(message.playerName));
					ITextComponent response = BankAccount.TransferCoins(menu, message.amount, destination);
					if(response != null)
						LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageBankTransferResponse(response));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
