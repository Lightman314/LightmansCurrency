package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.EjectionRecoveryMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketOpenEjectionMenu extends ClientToServerPacket {

	public static final Handler<CPacketOpenEjectionMenu> HANDLER = new H();

	private CPacketOpenEjectionMenu() {}

	public static void sendToServer() { new CPacketOpenEjectionMenu().send(); }

	@Override
	public void encode(FriendlyByteBuf buffer) { }

	private static class H extends Handler<CPacketOpenEjectionMenu>
	{
		@Override
		public CPacketOpenEjectionMenu decode(FriendlyByteBuf buffer) {
			LightmansCurrency.LogDebug("Decoded ejection packet!");
			return new CPacketOpenEjectionMenu();
		}

		@Override
		protected void handle(CPacketOpenEjectionMenu message, Player player) {
			LightmansCurrency.LogDebug("Opening ejection menu!");
			if(player instanceof ServerPlayer sp)
				NetworkHooks.openScreen(sp, EjectionRecoveryMenu.PROVIDER);
		}
	}
	
}
