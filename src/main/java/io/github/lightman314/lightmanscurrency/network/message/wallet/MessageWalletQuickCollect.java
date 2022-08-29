package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.wallet.WalletMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageWalletQuickCollect {

	public static void handle(MessageWalletQuickCollect message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof WalletMenu)
				{
					WalletMenu menu = (WalletMenu) player.containerMenu;
					menu.QuickCollectCoins();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
