package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketWalletToggleAutoExchange extends ClientToServerPacket {

	private static final Type<CPacketWalletToggleAutoExchange> TYPE = new Type<>(VersionUtil.lcResource("c_wallet_toggle_auto_exchange"));
	private static final CPacketWalletToggleAutoExchange INSTANCE = new CPacketWalletToggleAutoExchange();
	public static final Handler<CPacketWalletToggleAutoExchange> HANDLER = new H();

	private CPacketWalletToggleAutoExchange() { super(TYPE); }

	public static void sendToServer() { INSTANCE.send(); }

	private static class H extends SimpleHandler<CPacketWalletToggleAutoExchange>
	{
		protected H() { super(TYPE,INSTANCE); }
		@Override
		protected void handle(@Nonnull CPacketWalletToggleAutoExchange message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof WalletMenuBase menu)
				menu.ToggleAutoExchange();
		}
	}

}
