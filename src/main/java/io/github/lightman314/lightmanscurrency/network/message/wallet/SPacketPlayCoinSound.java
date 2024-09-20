package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketPlayCoinSound extends ServerToClientPacket.Simple {

	public static final SPacketPlayCoinSound INSTANCE = new SPacketPlayCoinSound();
	public static final Handler<SPacketPlayCoinSound> HANDLER = new H();

	private SPacketPlayCoinSound() {}

	private static class H extends SimpleHandler<SPacketPlayCoinSound>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(@Nonnull SPacketPlayCoinSound message, @Nullable ServerPlayer sender) {
			LightmansCurrency.getProxy().playCoinSound();
		}
	}

}
