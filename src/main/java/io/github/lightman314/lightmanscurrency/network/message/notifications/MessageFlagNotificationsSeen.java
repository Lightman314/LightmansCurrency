package io.github.lightman314.lightmanscurrency.network.message.notifications;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageFlagNotificationsSeen {

	NotificationCategory category;
	
	public MessageFlagNotificationsSeen(NotificationCategory category) {
		this.category = category;
	}
	
	public static void encode(MessageFlagNotificationsSeen message, PacketBuffer buffer) {
		buffer.writeNbt(message.category.save());
	}
	
	public static MessageFlagNotificationsSeen decode(PacketBuffer buffer) {
		return new MessageFlagNotificationsSeen(NotificationCategory.deserialize(buffer.readAnySizeNbt()));
	}
	
	public static void handle(MessageFlagNotificationsSeen message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->{
			PlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				NotificationData data = NotificationSaveData.GetNotifications(player);
				if(data != null && data.unseenNotification(message.category))
				{
					for(Notification n : data.getNotifications(message.category))
					{
						if(!n.wasSeen())
							n.setSeen();
					}
					NotificationSaveData.MarkNotificationsDirty(player.getUUID());
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}