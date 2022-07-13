package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.items.WalletItem.DataWriter;
import io.github.lightman314.lightmanscurrency.menus.providers.WalletMenuProvider;
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
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				IWalletHandler walletHandler = WalletCapability.getWalletHandler(player).orElse(null);
				if(walletHandler != null && !walletHandler.getWallet().isEmpty())
					NetworkHooks.openScreen(player, new WalletMenuProvider(message.walletStackIndex), new DataWriter(message.walletStackIndex));
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
