package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketWalletExchangeCoins extends ClientToServerPacket.Simple {

	private static final CPacketWalletExchangeCoins INSTANCE = new CPacketWalletExchangeCoins();
	public static final Handler<CPacketWalletExchangeCoins> HANDLER = new H();

	public static void sendToServer() { INSTANCE.send(); }

	private CPacketWalletExchangeCoins() {}

	private static class H extends SimpleHandler<CPacketWalletExchangeCoins>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(CPacketWalletExchangeCoins message, Player player) {
            if(player.containerMenu instanceof WalletMenuBase menu)
                menu.ExchangeCoins();
		}
	}

}
