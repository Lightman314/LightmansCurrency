package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageBankTransferPlayer {
	
	String playerName;
	CoinValue amount;
	
	public MessageBankTransferPlayer(String playerName, CoinValue amount) {
		this.playerName = playerName;
		this.amount = amount;
	}
	
	public static void encode(MessageBankTransferPlayer message, FriendlyByteBuf buffer) {
		buffer.writeUtf(message.playerName);
		buffer.writeNbt(message.amount.save(new CompoundTag(), CoinValue.DEFAULT_KEY));
	}

	public static MessageBankTransferPlayer decode(FriendlyByteBuf buffer) {
		return new MessageBankTransferPlayer(buffer.readUtf(), new CoinValue(buffer.readAnySizeNbt()));
	}

	public static void handle(MessageBankTransferPlayer message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof IBankAccountAdvancedMenu)
				{
					IBankAccountAdvancedMenu menu = (IBankAccountAdvancedMenu) player.containerMenu;
					AccountReference destination = BankAccount.GenerateReference(false, PlayerReference.of(false, message.playerName));
					MutableComponent response = BankAccount.TransferCoins(menu, message.amount, destination);
					if(response != null)
						LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageBankTransferResponse(response));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
