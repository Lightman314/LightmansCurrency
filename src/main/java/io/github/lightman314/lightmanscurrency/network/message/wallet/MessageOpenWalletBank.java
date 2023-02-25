package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem.DataWriter;
import io.github.lightman314.lightmanscurrency.common.menus.providers.WalletBankMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkHooks;

public class MessageOpenWalletBank {
	
	private final int walletStackIndex;
	
	public MessageOpenWalletBank(int walletStackIndex) { this.walletStackIndex = walletStackIndex;  }
	
	public static void encode(MessageOpenWalletBank message, FriendlyByteBuf buffer) { buffer.writeInt(message.walletStackIndex); }

	public static MessageOpenWalletBank decode(FriendlyByteBuf buffer) {
		return new MessageOpenWalletBank(buffer.readInt());
	}

	public static void handle(MessageOpenWalletBank message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
				NetworkHooks.openScreen(player, new WalletBankMenuProvider(message.walletStackIndex), new DataWriter(message.walletStackIndex));
		});
		supplier.get().setPacketHandled(true);
	}

}
