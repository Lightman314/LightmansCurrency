package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketPlayCoinSound extends ServerToClientPacket {

	private static final Type<SPacketPlayCoinSound> TYPE = new Type<>(VersionUtil.lcResource("s_play_pickup_sound"));
	public static final SPacketPlayCoinSound INSTANCE = new SPacketPlayCoinSound();
	public static final Handler<SPacketPlayCoinSound> HANDLER = new H();

	private SPacketPlayCoinSound() { super(TYPE); }

	private static class H extends SimpleHandler<SPacketPlayCoinSound>
	{
		protected H() { super(TYPE,INSTANCE); }
		@Override
		protected void handle(@Nonnull SPacketPlayCoinSound message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.getProxy().playCoinSound();
		}
	}

}
