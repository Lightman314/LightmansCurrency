package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageBankTransferTeam {
	
	long teamID;
	CoinValue amount;
	
	public MessageBankTransferTeam(long teamID, CoinValue amount) {
		this.teamID = teamID;
		this.amount = amount;
	}
	
	public static void encode(MessageBankTransferTeam message, PacketBuffer buffer) {
		buffer.writeLong(message.teamID);
		message.amount.encode(buffer);
	}

	public static MessageBankTransferTeam decode(PacketBuffer buffer) {
		return new MessageBankTransferTeam(buffer.readLong(), CoinValue.decode(buffer));
	}

	public static void handle(MessageBankTransferTeam message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof IBankAccountAdvancedMenu)
				{
					IBankAccountAdvancedMenu menu = (IBankAccountAdvancedMenu) player.containerMenu;
					AccountReference destination = BankAccount.GenerateReference(false, message.teamID);
					IFormattableTextComponent response = BankAccount.TransferCoins(menu, message.amount, destination);
					if(response != null)
						LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageBankTransferResponse(response));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}