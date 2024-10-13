package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import io.github.lightman314.lightmanscurrency.common.menus.EjectionRecoveryMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketOpenEjectionMenu extends ClientToServerPacket.Simple {

	private static final CPacketOpenEjectionMenu INSTANCE = new CPacketOpenEjectionMenu();
	public static final Handler<CPacketOpenEjectionMenu> HANDLER = new H();

	private CPacketOpenEjectionMenu() {}

	public static void sendToServer() { INSTANCE.send(); }

	private static class H extends SimpleHandler<CPacketOpenEjectionMenu>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(@Nonnull CPacketOpenEjectionMenu message, @Nullable ServerPlayer sender) {
			if(sender != null)
				NetworkHooks.openGui(sender, EjectionRecoveryMenu.PROVIDER);
		}
	}
	
}
