package io.github.lightman314.lightmanscurrency.network.message.notifications;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketChatNotification extends ServerToClientPacket {

	public static final Handler<SPacketChatNotification> HANDLER = new H();

	public Notification notification;
	
	public SPacketChatNotification(Notification notification) { this.notification = notification; }
	
	public void encode(FriendlyByteBuf buffer) { buffer.writeNbt(this.notification.save()); }

	private static class H extends Handler<SPacketChatNotification>
	{
		@Override
		public SPacketChatNotification decode(FriendlyByteBuf buffer) { return new SPacketChatNotification(NotificationAPI.getApi().LoadNotification(buffer.readAnySizeNbt())); }
		@Override
		protected void handle(SPacketChatNotification message, Player player) {
			LightmansCurrency.getProxy().receiveNotification(message.notification);
		}
	}
	
}
