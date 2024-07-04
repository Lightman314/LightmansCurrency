package io.github.lightman314.lightmanscurrency.network.message.notifications;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketFlagNotificationsSeen extends ClientToServerPacket {

	private static final Type<CPacketFlagNotificationsSeen> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_notification_flag_seen"));
	public static final Handler<CPacketFlagNotificationsSeen> HANDLER = new H();

	private final NotificationCategory category;
	
	public CPacketFlagNotificationsSeen(NotificationCategory category) { super(TYPE); this.category = category; }

	private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull CPacketFlagNotificationsSeen message) { buffer.writeNbt(message.category.save(buffer.registryAccess())); }
	private static CPacketFlagNotificationsSeen decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new CPacketFlagNotificationsSeen(NotificationAPI.loadCategory(readNBT(buffer),buffer.registryAccess())); }

	private static class H extends Handler<CPacketFlagNotificationsSeen>
	{
		protected H() { super(TYPE, fancyCodec(CPacketFlagNotificationsSeen::encode,CPacketFlagNotificationsSeen::decode)); }
		@Override
		protected void handle(@Nonnull CPacketFlagNotificationsSeen message, @Nonnull IPayloadContext context, @Nonnull Player player) {
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
	}
	
}
