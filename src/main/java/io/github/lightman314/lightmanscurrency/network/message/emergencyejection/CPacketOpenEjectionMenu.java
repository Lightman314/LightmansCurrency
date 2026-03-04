package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import io.github.lightman314.lightmanscurrency.common.menus.EjectionRecoveryMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketOpenEjectionMenu extends ClientToServerPacket {


	private static final Type<CPacketOpenEjectionMenu> TYPE = cType("ejection_data_open");
	private static final CPacketOpenEjectionMenu INSTANCE = new CPacketOpenEjectionMenu();
	public static final Handler<CPacketOpenEjectionMenu> HANDLER = new H();

	private CPacketOpenEjectionMenu() { super(TYPE); }

	public static void send() { INSTANCE.sendToServer(); }

	private static class H extends SimpleHandler<CPacketOpenEjectionMenu>
	{
		protected H() { super(TYPE, INSTANCE); }
		@Override
		protected void handle(CPacketOpenEjectionMenu message, IPayloadContext context, Player player) {
			player.openMenu(EjectionRecoveryMenu.PROVIDER);
		}
	}
	
}
