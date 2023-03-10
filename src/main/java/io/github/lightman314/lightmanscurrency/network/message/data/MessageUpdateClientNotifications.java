package io.github.lightman314.lightmanscurrency.network.message.data;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageUpdateClientNotifications {

	public NotificationData data;
	
	public MessageUpdateClientNotifications(NotificationData data) {
		this.data = data;
	}
	
	public static void encode(MessageUpdateClientNotifications message, PacketBuffer buffer) {
		buffer.writeNbt(message.data.save());
	}

	public static MessageUpdateClientNotifications decode(PacketBuffer buffer) {
		return new MessageUpdateClientNotifications(NotificationData.loadFrom(buffer.readNbt()));
	}

	public static void handle(MessageUpdateClientNotifications message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateNotifications(message.data));
		supplier.get().setPacketHandled(true);
	}
	
}
