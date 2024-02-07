package io.github.lightman314.lightmanscurrency.network.message.data.team;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketClearClientTeams extends ServerToClientPacket.Simple {

	public static final SPacketClearClientTeams INSTANCE = new SPacketClearClientTeams();
	public static final Handler<SPacketClearClientTeams> HANDLER = new H();

	private SPacketClearClientTeams() {}

	private static class H extends SimpleHandler<SPacketClearClientTeams>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(@Nonnull SPacketClearClientTeams message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.clearTeams();
		}
	}

}
