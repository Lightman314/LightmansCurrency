package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageATMSetAccount {
	
	private AccountReference account;
	
	public MessageATMSetAccount(AccountReference account)
	{
		this.account = account;
	}
	
	public static void encode(MessageATMSetAccount message, PacketBuffer buffer) {
		message.account.writeToBuffer(buffer);
	}

	public static MessageATMSetAccount decode(PacketBuffer buffer) {
		return new MessageATMSetAccount(BankAccount.LoadReference(false, buffer));
	}

	public static void handle(MessageATMSetAccount message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ATMContainer)
				{
					ATMContainer container = (ATMContainer) entity.openContainer;
					container.SetAccount(message.account);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
