package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageSetBankNotificationLevel {
	
	CoinValue amount;
	
	public MessageSetBankNotificationLevel(CoinValue amount) {
		this.amount = amount;
	}
	
	public static void encode(MessageSetBankNotificationLevel message, PacketBuffer buffer) {
		message.amount.encode(buffer);
	}

	public static MessageSetBankNotificationLevel decode(PacketBuffer buffer) {
		return new MessageSetBankNotificationLevel(CoinValue.decode(buffer));
	}

	public static void handle(MessageSetBankNotificationLevel message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof IBankAccountAdvancedMenu)
				{
					IBankAccountAdvancedMenu menu = (IBankAccountAdvancedMenu) player.containerMenu;
					menu.setNotificationLevel(message.amount);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
