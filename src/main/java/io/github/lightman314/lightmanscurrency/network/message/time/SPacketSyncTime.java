package io.github.lightman314.lightmanscurrency.network.message.time;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketSyncTime extends ServerToClientPacket {

	private static final Type<SPacketSyncTime> TYPE = sType("sync_time");
    private static final StreamCodec<ByteBuf,SPacketSyncTime> STREAM_CODEC = ByteBufCodecs.VAR_LONG
            .map(SPacketSyncTime::new,p -> p.time);
	public static final Handler<SPacketSyncTime> HANDLER = new H();

	public static void syncWith(Player player) { new SPacketSyncTime(TimeUtil.getCurrentTime()).sendTo(player); }

	private final long time;
	private SPacketSyncTime(long time) { super(TYPE); this.time = time; }

	private static class H extends Handler<SPacketSyncTime>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(SPacketSyncTime message, IPayloadContext context, Player player) {
			LightmansCurrency.getProxy().setTimeDesync(message.time);
		}
	}

}
