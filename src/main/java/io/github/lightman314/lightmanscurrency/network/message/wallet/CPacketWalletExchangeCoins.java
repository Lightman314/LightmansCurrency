package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketWalletExchangeCoins extends ClientToServerPacket {

	private static final Type<CPacketWalletExchangeCoins> TYPE = new Type<>(VersionUtil.lcResource("c_wallet_exchange"));
	private static final CPacketWalletExchangeCoins INSTANCE = new CPacketWalletExchangeCoins();
	public static final Handler<CPacketWalletExchangeCoins> HANDLER = new H();

	public static void sendToServer() { INSTANCE.send(); }

	private CPacketWalletExchangeCoins() { super(TYPE); }

	private static class H extends SimpleHandler<CPacketWalletExchangeCoins>
	{
		protected H() { super(TYPE,INSTANCE); }
		@Override
		protected void handle(@Nonnull CPacketWalletExchangeCoins message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof WalletMenuBase menu)
				menu.ExchangeCoins();
		}
	}

}
