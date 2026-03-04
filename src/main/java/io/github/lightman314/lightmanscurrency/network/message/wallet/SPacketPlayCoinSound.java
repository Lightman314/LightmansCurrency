package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketPlayCoinSound extends ServerToClientPacket {

	private static final Type<SPacketPlayCoinSound> TYPE = sType("play_pickup_sound");
	public static final SPacketPlayCoinSound INSTANCE = new SPacketPlayCoinSound();
	public static final Handler<SPacketPlayCoinSound> HANDLER = new H();

	private SPacketPlayCoinSound() { super(TYPE); }

	private static class H extends SimpleHandler<SPacketPlayCoinSound>
	{
		protected H() { super(TYPE,INSTANCE); }
		@Override
		protected void handle(SPacketPlayCoinSound message, IPayloadContext context, Player player) {
			LightmansCurrency.getProxy().playCoinSound();
		}
	}

}
