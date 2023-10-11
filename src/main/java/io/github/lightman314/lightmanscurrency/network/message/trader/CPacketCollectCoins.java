package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.menus.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketCollectCoins extends ClientToServerPacket.Simple {

	private static final CPacketCollectCoins INSTANCE = new CPacketCollectCoins();
	public static final Handler<CPacketCollectCoins> HANDLER = new H();

	private CPacketCollectCoins() {}

	public static void sendToServer() { INSTANCE.send(); }

	private static class H extends SimpleHandler<CPacketCollectCoins>
	{
		protected H() { super(INSTANCE); }

		@Override
		protected void handle(@Nonnull CPacketCollectCoins message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				if(sender.containerMenu instanceof TraderMenu menu)
					menu.CollectCoinStorage();
				else if(sender.containerMenu instanceof TraderStorageMenu menu)
					menu.CollectCoinStorage();
				else if(sender.containerMenu instanceof SlotMachineMenu menu)
					menu.CollectCoinStorage();
			}
		}
	}

}
