package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketStoreCoins extends ClientToServerPacket {

	private static final Type<CPacketStoreCoins> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_trader_store_coins"));
	private static final CPacketStoreCoins INSTANCE = new CPacketStoreCoins();
	public static final Handler<CPacketStoreCoins> HANDLER = new H();

	private CPacketStoreCoins() { super(TYPE); }

	public static void sendToServer() { INSTANCE.send(); }

	private static class H extends SimpleHandler<CPacketStoreCoins>
	{
		protected H() { super(TYPE, INSTANCE); }
		@Override
		protected void handle(@Nonnull CPacketStoreCoins message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof TraderStorageMenu menu)
				menu.AddCoins();
		}
	}

}
