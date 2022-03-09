package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountType;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountTransferMenu;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageBankTransferTeam {
	
	UUID teamID;
	CoinValue amount;
	
	public MessageBankTransferTeam(UUID teamID, CoinValue amount) {
		this.teamID = teamID;
		this.amount = amount;
	}
	
	public static void encode(MessageBankTransferTeam message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.teamID);
		buffer.writeCompoundTag(message.amount.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
	}

	public static MessageBankTransferTeam decode(PacketBuffer buffer) {
		return new MessageBankTransferTeam(buffer.readUniqueId(), new CoinValue(buffer.readCompoundTag()));
	}

	public static void handle(MessageBankTransferTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.openContainer instanceof IBankAccountTransferMenu)
				{
					IBankAccountTransferMenu menu = (IBankAccountTransferMenu) player.openContainer;
					AccountReference destination = BankAccount.GenerateReference(false, AccountType.Team, message.teamID);
					ITextComponent response = BankAccount.TransferCoins(menu, message.amount, destination);
					if(response != null)
						LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageBankTransferResponse(response));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
