package io.github.lightman314.lightmanscurrency.network.message.notifications;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketChatNotification extends ServerToClientPacket {

	public static final Handler<SPacketChatNotification> HANDLER = new H();

	public Notification notification;
	
	public SPacketChatNotification(Notification notification) { this.notification = notification; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeNbt(this.notification.save()); }

	private static class H extends Handler<SPacketChatNotification>
	{
		@Nonnull
		@Override
		public SPacketChatNotification decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketChatNotification(Notification.deserialize(buffer.readNbt())); }
		@Override
		protected void handle(@Nonnull SPacketChatNotification message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.receiveNotification(message.notification);
		}
	}
	
}
