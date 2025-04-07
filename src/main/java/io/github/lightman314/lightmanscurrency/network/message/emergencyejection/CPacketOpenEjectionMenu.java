package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import io.github.lightman314.lightmanscurrency.common.menus.EjectionRecoveryMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketOpenEjectionMenu extends ClientToServerPacket {


	private static final Type<CPacketOpenEjectionMenu> TYPE = new Type<>(VersionUtil.lcResource("c_ejection_data_open"));
	private static final CPacketOpenEjectionMenu INSTANCE = new CPacketOpenEjectionMenu();
	public static final Handler<CPacketOpenEjectionMenu> HANDLER = new H();

	private CPacketOpenEjectionMenu() { super(TYPE); }

	public static void sendToServer() { new CPacketOpenEjectionMenu().send(); }

	private static class H extends SimpleHandler<CPacketOpenEjectionMenu>
	{
		protected H() { super(TYPE, INSTANCE); }
		@Override
		protected void handle(@Nonnull CPacketOpenEjectionMenu message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			player.openMenu(EjectionRecoveryMenu.PROVIDER);
		}
	}
	
}
