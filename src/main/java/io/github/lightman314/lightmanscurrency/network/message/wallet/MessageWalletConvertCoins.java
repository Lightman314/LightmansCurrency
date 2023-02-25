package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageWalletConvertCoins {
	
	public static void encode(MessageWalletConvertCoins message, FriendlyByteBuf buffer) { }

	public static MessageWalletConvertCoins decode(FriendlyByteBuf buffer) {
		return new MessageWalletConvertCoins();
	}

	public static void handle(MessageWalletConvertCoins message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof WalletMenuBase)
				{
					WalletMenuBase menu = (WalletMenuBase) player.containerMenu;
					menu.ConvertCoins();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
