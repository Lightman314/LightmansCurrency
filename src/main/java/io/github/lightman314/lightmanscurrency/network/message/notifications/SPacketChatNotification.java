package io.github.lightman314.lightmanscurrency.network.message.notifications;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketChatNotification extends ServerToClientPacket {

	private static final Type<SPacketChatNotification> TYPE = new Type<>(VersionUtil.lcResource("s_notification_chat"));
	public static final Handler<SPacketChatNotification> HANDLER = new H();

	private final Notification notification;
	
	public SPacketChatNotification(Notification notification) { super(TYPE); this.notification = notification; }

	private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull SPacketChatNotification message) { buffer.writeNbt(message.notification.save(buffer.registryAccess())); }
	private static SPacketChatNotification decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new SPacketChatNotification(NotificationAPI.API.LoadNotification(readNBT(buffer),buffer.registryAccess())); }

	private static class H extends Handler<SPacketChatNotification>
	{
		protected H() { super(TYPE, fancyCodec(SPacketChatNotification::encode,SPacketChatNotification::decode)); }
		@Override
		protected void handle(@Nonnull SPacketChatNotification message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.getProxy().receiveNotification(message.notification);
		}
	}
	
}
