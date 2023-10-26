package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketPlayPickupSound extends ServerToClientPacket.Simple {

	public static final SPacketPlayPickupSound INSTANCE = new SPacketPlayPickupSound();
	public static final Handler<SPacketPlayPickupSound> HANDLER = new H();

	private SPacketPlayPickupSound() {}

	private static class H extends SimpleHandler<SPacketPlayPickupSound>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(@Nonnull SPacketPlayPickupSound message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.playCoinSound();
		}
	}

}
