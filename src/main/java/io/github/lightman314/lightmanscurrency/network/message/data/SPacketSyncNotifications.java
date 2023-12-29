package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketSyncNotifications extends ServerToClientPacket {

	public static final Handler<SPacketSyncNotifications> HANDLER = new H();

	public NotificationData data;
	
	public SPacketSyncNotifications(NotificationData data) { this.data = data; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeNbt(this.data.save()); }

	private static class H extends Handler<SPacketSyncNotifications>
	{
		@Nonnull
		@Override
		public SPacketSyncNotifications decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncNotifications(NotificationData.loadFrom(buffer.readAnySizeNbt())); }
		@Override
		protected void handle(@Nonnull SPacketSyncNotifications message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.updateNotifications(message.data);
		}
	}
	
}
