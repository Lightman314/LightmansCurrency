package io.github.lightman314.lightmanscurrency.network.message.data;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageUpdateClientNotifications {

	public NotificationData data;
	
	public MessageUpdateClientNotifications(NotificationData data) {
		this.data = data;
	}
	
	public static void encode(MessageUpdateClientNotifications message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.data.save());
	}

	public static MessageUpdateClientNotifications decode(FriendlyByteBuf buffer) {
		return new MessageUpdateClientNotifications(NotificationData.loadFrom(buffer.readNbt()));
	}

	public static void handle(MessageUpdateClientNotifications message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateNotifications(message.data));
		supplier.get().setPacketHandled(true);
	}
	
}
