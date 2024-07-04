package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketWalletQuickCollect extends ClientToServerPacket {

	private static final Type<CPacketWalletQuickCollect> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_wallet_quick_collect"));
	private static final CPacketWalletQuickCollect INSTANCE = new CPacketWalletQuickCollect();
	public static final Handler<CPacketWalletQuickCollect> HANDLER = new H();

	private CPacketWalletQuickCollect() { super(TYPE); }

	public static void sendToServer() { INSTANCE.send(); }

	private static class H extends SimpleHandler<CPacketWalletQuickCollect>
	{
		protected H() { super(TYPE,INSTANCE); }
		@Override
		protected void handle(@Nonnull CPacketWalletQuickCollect message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof WalletMenu menu)
				menu.QuickCollectCoins();
		}
	}

}
