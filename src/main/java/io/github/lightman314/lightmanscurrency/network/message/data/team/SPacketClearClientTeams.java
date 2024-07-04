package io.github.lightman314.lightmanscurrency.network.message.data.team;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketClearClientTeams extends ServerToClientPacket {

	private static final Type<SPacketClearClientTeams> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_team_clear_client"));
	public static final SPacketClearClientTeams INSTANCE = new SPacketClearClientTeams();
	public static final Handler<SPacketClearClientTeams> HANDLER = new H();

	private SPacketClearClientTeams() { super(TYPE); }

	private static class H extends SimpleHandler<SPacketClearClientTeams>
	{
		protected H() { super(TYPE,INSTANCE); }
		@Override
		protected void handle(@Nonnull SPacketClearClientTeams message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.PROXY.clearTeams();
		}
	}

}
