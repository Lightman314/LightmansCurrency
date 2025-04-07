package io.github.lightman314.lightmanscurrency.network.message.time;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketSyncTime extends ServerToClientPacket {

	private static final Type<SPacketSyncTime> TYPE = new Type<>(VersionUtil.lcResource("s_sync_time"));
	public static final Handler<SPacketSyncTime> HANDLER = new H();

	public static void syncWith(@Nonnull Player player) { new SPacketSyncTime(TimeUtil.getCurrentTime()).sendTo(player); }

	private final long time;
	private SPacketSyncTime(long time) { super(TYPE); this.time = time; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketSyncTime message) { buffer.writeLong(TimeUtil.getCurrentTime()); }
	private static SPacketSyncTime decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncTime(buffer.readLong()); }

	private static class H extends Handler<SPacketSyncTime>
	{
		protected H() { super(TYPE, easyCodec(SPacketSyncTime::encode,SPacketSyncTime::decode)); }
		@Override
		protected void handle(@Nonnull SPacketSyncTime message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.getProxy().setTimeDesync(message.time);
		}
	}

}
