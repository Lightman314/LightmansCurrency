package io.github.lightman314.lightmanscurrency.network.message.time;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketSyncTime extends ServerToClientPacket {

	public static final Handler<SPacketSyncTime> HANDLER = new H();

	public static void syncWith(@Nonnull PacketDistributor.PacketTarget player) { new SPacketSyncTime(0).sendToTarget(player); }

	private final long time;
	private SPacketSyncTime(long time) { this.time = time; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeLong(TimeUtil.getCurrentTime()); }

	private static class H extends Handler<SPacketSyncTime>
	{
		@Nonnull
		@Override
		public SPacketSyncTime decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncTime(buffer.readLong()); }
		@Override
		protected void handle(@Nonnull SPacketSyncTime message, @Nullable ServerPlayer sender) {
			LightmansCurrency.getProxy().setTimeDesync(message.time);
		}
	}

}
