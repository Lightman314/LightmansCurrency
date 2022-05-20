package io.github.lightman314.lightmanscurrency.network.message.notifications;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageClientNotification {

	public Notification notification;
	
	public MessageClientNotification(Notification notification) {
		this.notification = notification;
	}
	
	public static void encode(MessageClientNotification message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.notification.save());
	}

	public static MessageClientNotification decode(FriendlyByteBuf buffer) {
		return new MessageClientNotification(Notification.deserialize(buffer.readNbt()));
	}

	public static void handle(MessageClientNotification message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.receiveNotification(message.notification));
		supplier.get().setPacketHandled(true);
	}
	
}
