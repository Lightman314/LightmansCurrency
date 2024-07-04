package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.menu.IMoneyCollectionMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketCollectCoins extends ClientToServerPacket {

	private static final Type<CPacketCollectCoins> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_trader_collect_coins"));
	private static final CPacketCollectCoins INSTANCE = new CPacketCollectCoins();
	public static final Handler<CPacketCollectCoins> HANDLER = new H();

	private CPacketCollectCoins() { super(TYPE); }

	public static void sendToServer() { INSTANCE.send(); }

	private static class H extends SimpleHandler<CPacketCollectCoins>
	{
		protected H() { super(TYPE, INSTANCE); }
		@Override
		protected void handle(@Nonnull CPacketCollectCoins message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof IMoneyCollectionMenu menu)
				menu.CollectStoredMoney();
		}
	}

}
