package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetBankNotificationLevel {
	
	CoinValue amount;
	
	public MessageSetBankNotificationLevel(CoinValue amount) {
		this.amount = amount;
	}
	
	public static void encode(MessageSetBankNotificationLevel message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.amount.save(new CompoundTag(), CoinValue.DEFAULT_KEY));
	}

	public static MessageSetBankNotificationLevel decode(FriendlyByteBuf buffer) {
		return new MessageSetBankNotificationLevel(new CoinValue(buffer.readAnySizeNbt()));
	}

	public static void handle(MessageSetBankNotificationLevel message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
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
