package io.github.lightman314.lightmanscurrency.network.message.notifications;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketChatNotification extends ServerToClientPacket {

	private static final Type<SPacketChatNotification> TYPE = sType("notification_chat");
    private static final StreamCodec<RegistryFriendlyByteBuf,SPacketChatNotification> STREAM_CODEC = Notification.STREAM_CODEC
            .map(SPacketChatNotification::new,p -> p.notification);
	public static final Handler<SPacketChatNotification> HANDLER = new H();

	private final Notification notification;
	
	public SPacketChatNotification(Notification notification) { super(TYPE); this.notification = notification; }
	private static class H extends Handler<SPacketChatNotification>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(SPacketChatNotification message, IPayloadContext context, Player player) {
			LightmansCurrency.getProxy().receiveNotification(message.notification);
		}
	}
	
}
