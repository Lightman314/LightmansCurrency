package io.github.lightman314.lightmanscurrency.network.message.notifications;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification.Category;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageFlagNotificationsSeen {

	Category category;
	
	public MessageFlagNotificationsSeen(Category category) {
		this.category = category;
	}
	
	public static void encode(MessageFlagNotificationsSeen message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.category.save());
	}
	
	public static MessageFlagNotificationsSeen decode(FriendlyByteBuf buffer) {
		return new MessageFlagNotificationsSeen(Category.deserialize(buffer.readAnySizeNbt()));
	}
	
	public static void handle(MessageFlagNotificationsSeen message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->{
			Player player = supplier.get().getSender();
			if(player != null)
			{
				NotificationData data = TradingOffice.getNotifications(player);
				if(data != null && data.unseenNotification(message.category))
				{
					for(Notification n : data.getNotifications(message.category))
					{
						if(!n.wasSeen())
							n.setSeen();
					}
					TradingOffice.MarkNotificationsDirty(player.getUUID());
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
