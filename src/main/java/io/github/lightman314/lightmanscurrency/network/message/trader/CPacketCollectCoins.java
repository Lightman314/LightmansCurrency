package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.api.traders.menu.IMoneyCollectionMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.world.entity.player.Player;

public class CPacketCollectCoins extends ClientToServerPacket.Simple {

	private static final CPacketCollectCoins INSTANCE = new CPacketCollectCoins();
	public static final Handler<CPacketCollectCoins> HANDLER = new H();

	private CPacketCollectCoins() {}

	public static void sendToServer() { INSTANCE.send(); }

	private static class H extends SimpleHandler<CPacketCollectCoins>
	{
		protected H() { super(INSTANCE); }

		@Override
		protected void handle(CPacketCollectCoins message, Player player) {
            if(player.containerMenu instanceof IMoneyCollectionMenu menu)
                menu.CollectStoredMoney();
		}
	}

}
