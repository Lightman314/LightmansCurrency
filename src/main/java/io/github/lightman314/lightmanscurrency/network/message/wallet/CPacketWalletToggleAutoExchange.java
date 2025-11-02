package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketWalletToggleAutoExchange extends ClientToServerPacket.Simple {

	private static final CPacketWalletToggleAutoExchange INSTANCE = new CPacketWalletToggleAutoExchange();
	public static final Handler<CPacketWalletToggleAutoExchange> HANDLER = new H();

	private CPacketWalletToggleAutoExchange() {}

	public static void sendToServer() { INSTANCE.send(); }

	private static class H extends SimpleHandler<CPacketWalletToggleAutoExchange>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(CPacketWalletToggleAutoExchange message, Player player) {
            if(player.containerMenu instanceof WalletMenuBase menu)
                menu.ToggleAutoExchange();
		}
	}

}
