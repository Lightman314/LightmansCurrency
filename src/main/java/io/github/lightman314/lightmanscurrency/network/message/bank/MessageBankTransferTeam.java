package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountType;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageBankTransferTeam {
	
	UUID teamID;
	CoinValue amount;
	
	public MessageBankTransferTeam(UUID teamID, CoinValue amount) {
		this.teamID = teamID;
		this.amount = amount;
	}
	
	public static void encode(MessageBankTransferTeam message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.teamID);
		buffer.writeNbt(message.amount.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
	}

	public static MessageBankTransferTeam decode(FriendlyByteBuf buffer) {
		return new MessageBankTransferTeam(buffer.readUUID(), new CoinValue(buffer.readAnySizeNbt()));
	}

	public static void handle(MessageBankTransferTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof IBankAccountAdvancedMenu)
				{
					IBankAccountAdvancedMenu menu = (IBankAccountAdvancedMenu) player.containerMenu;
					AccountReference destination = BankAccount.GenerateReference(false, AccountType.Team, message.teamID);
					Component response = BankAccount.TransferCoins(menu, message.amount, destination);
					if(response != null)
						LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageBankTransferResponse(response));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
