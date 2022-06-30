package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.wallet.WalletMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageWalletToggleAutoConvert {
	
	public static void encode(MessageWalletToggleAutoConvert message, FriendlyByteBuf buffer) { }

	public static MessageWalletToggleAutoConvert decode(FriendlyByteBuf buffer) {
		return new MessageWalletToggleAutoConvert();
	}

	public static void handle(MessageWalletToggleAutoConvert message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof WalletMenu)
				{
					WalletMenu menu = (WalletMenu) player.containerMenu;
					menu.ToggleAutoConvert();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
