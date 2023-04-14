package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

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
				WalletMenuBase.SafeOpenWalletBankMenu(player, message.walletStackIndex);
		});
		supplier.get().setPacketHandled(true);
	}

}
