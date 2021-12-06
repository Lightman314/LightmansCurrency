package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.providers.WalletContainerProvider;
import io.github.lightman314.lightmanscurrency.items.WalletItem.DataWriter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkHooks;

public class MessageOpenWallet {
	
	public static void encode(MessageOpenWallet message, FriendlyByteBuf buffer) { }

	public static MessageOpenWallet decode(FriendlyByteBuf buffer) {
		return new MessageOpenWallet();
	}

	public static void handle(MessageOpenWallet message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				NetworkHooks.openGui(player, new WalletContainerProvider(-1), new DataWriter(-1));
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
