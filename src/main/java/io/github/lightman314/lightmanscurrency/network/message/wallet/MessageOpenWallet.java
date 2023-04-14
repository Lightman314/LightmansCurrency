package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

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
				WalletMenuBase.SafeOpenWalletMenu(player, message.walletStackIndex);
		});
		supplier.get().setPacketHandled(true);
	}

}
