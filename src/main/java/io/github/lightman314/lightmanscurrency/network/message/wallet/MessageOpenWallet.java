package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem.DataWriter;
import io.github.lightman314.lightmanscurrency.common.menus.providers.WalletMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkHooks;

public class MessageOpenWallet {
	
	private final int walletStackIndex;
	
	public MessageOpenWallet(int walletStackIndex) { this.walletStackIndex = walletStackIndex;  }
	
	public static void encode(MessageOpenWallet message, FriendlyByteBuf buffer) { buffer.writeInt(message.walletStackIndex); }

	public static MessageOpenWallet decode(FriendlyByteBuf buffer) {
		return new MessageOpenWallet(buffer.readInt());
	}

	public static void handle(MessageOpenWallet message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//Don't need to check for valid wallet handlers as the wallet stack index might not be -1...
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
				NetworkHooks.openGui(player, new WalletMenuProvider(message.walletStackIndex), new DataWriter(message.walletStackIndex));
		});
		supplier.get().setPacketHandled(true);
	}

}
