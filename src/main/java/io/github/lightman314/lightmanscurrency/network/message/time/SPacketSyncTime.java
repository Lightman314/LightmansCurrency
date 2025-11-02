package io.github.lightman314.lightmanscurrency.network.message.time;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketSyncTime extends ServerToClientPacket {

	public static final Handler<SPacketSyncTime> HANDLER = new H();

	public static void syncWith(PacketDistributor.PacketTarget player) { new SPacketSyncTime(0).sendToTarget(player); }

	private final long time;
	private SPacketSyncTime(long time) { this.time = time; }
	
	public void encode(FriendlyByteBuf buffer) { buffer.writeLong(TimeUtil.getCurrentTime()); }

	private static class H extends Handler<SPacketSyncTime>
	{
		@Override
		public SPacketSyncTime decode(FriendlyByteBuf buffer) { return new SPacketSyncTime(buffer.readLong()); }
		@Override
		protected void handle(SPacketSyncTime message, Player player) {
			LightmansCurrency.getProxy().setTimeDesync(message.time);
		}
	}

}
