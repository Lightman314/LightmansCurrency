package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketStoreCoins extends ClientToServerPacket.Simple {

	private static final CPacketStoreCoins INSTANCE = new CPacketStoreCoins();
	public static final Handler<CPacketStoreCoins> HANDLER = new H();

	private CPacketStoreCoins() {}

	public static void sendToServer() { INSTANCE.send(); }

	private static class H extends SimpleHandler<CPacketStoreCoins>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(@Nonnull CPacketStoreCoins message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				if(sender.containerMenu instanceof TraderStorageMenu menu)
					menu.AddCoins();
			}
		}
	}

}
