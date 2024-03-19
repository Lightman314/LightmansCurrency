package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.EjectionRecoveryMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketOpenEjectionMenu extends ClientToServerPacket {

	public static final Handler<CPacketOpenEjectionMenu> HANDLER = new H();

	private CPacketOpenEjectionMenu() {}

	public static void sendToServer() { new CPacketOpenEjectionMenu().send(); }

	@Override
	public void encode(@Nonnull FriendlyByteBuf buffer) { }

	private static class H extends Handler<CPacketOpenEjectionMenu>
	{
		@Nonnull
		@Override
		public CPacketOpenEjectionMenu decode(@Nonnull FriendlyByteBuf buffer) {
			LightmansCurrency.LogDebug("Decoded ejection packet!");
			return new CPacketOpenEjectionMenu();
		}
		@Override
		protected void handle(@Nonnull CPacketOpenEjectionMenu message, @Nullable ServerPlayer sender) {
			LightmansCurrency.LogDebug("Opening ejection menu!");
			if(sender != null)
				NetworkHooks.openScreen(sender, EjectionRecoveryMenu.PROVIDER);
		}
	}
	
}
