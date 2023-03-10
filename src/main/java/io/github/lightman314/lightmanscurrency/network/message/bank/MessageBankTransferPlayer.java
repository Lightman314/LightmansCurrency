package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageBankTransferPlayer {
	
	String playerName;
	CoinValue amount;
	
	public MessageBankTransferPlayer(String playerName, CoinValue amount) {
		this.playerName = playerName;
		this.amount = amount;
	}
	
	public static void encode(MessageBankTransferPlayer message, PacketBuffer buffer) {
		buffer.writeUtf(message.playerName);
		message.amount.encode(buffer);
	}

	public static MessageBankTransferPlayer decode(PacketBuffer buffer) {
		return new MessageBankTransferPlayer(buffer.readUtf(), new CoinValue(buffer.readAnySizeNbt()));
	}

	public static void handle(MessageBankTransferPlayer message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof IBankAccountAdvancedMenu)
				{
					IBankAccountAdvancedMenu menu = (IBankAccountAdvancedMenu) player.containerMenu;
					AccountReference destination = BankAccount.GenerateReference(false, PlayerReference.of(false, message.playerName));
					IFormattableTextComponent response = BankAccount.TransferCoins(menu, message.amount, destination);
					if(response != null)
						LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageBankTransferResponse(response));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}