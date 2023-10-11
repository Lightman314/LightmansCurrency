package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketClearClientTraders extends ServerToClientPacket.Simple {

	public static final SPacketClearClientTraders INSTANCE = new SPacketClearClientTraders();
	public static final Handler<SPacketClearClientTraders> HANDLER = new H();

	private SPacketClearClientTraders() {}

	private static class H extends SimpleHandler<SPacketClearClientTraders>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(@Nonnull SPacketClearClientTraders message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.clearClientTraders();
		}
	}
	
}
