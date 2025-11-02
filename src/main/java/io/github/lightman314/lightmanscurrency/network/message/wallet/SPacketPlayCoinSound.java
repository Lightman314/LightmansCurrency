package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketPlayCoinSound extends ServerToClientPacket.Simple {

	public static final SPacketPlayCoinSound INSTANCE = new SPacketPlayCoinSound();
	public static final Handler<SPacketPlayCoinSound> HANDLER = new H();

	private SPacketPlayCoinSound() {}

	private static class H extends SimpleHandler<SPacketPlayCoinSound>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(SPacketPlayCoinSound message, Player player) {
			LightmansCurrency.getProxy().playCoinSound();
		}
	}

}
