package io.github.lightman314.lightmanscurrency.api.notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncNotifications;
import io.github.lightman314.lightmanscurrency.network.message.notifications.SPacketChatNotification;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = LightmansCurrency.MODID)
public class NotificationSaveData extends SavedData {

	private NotificationSaveData() {}
	
	private NotificationSaveData(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		ListTag notificationData = compound.getList("PlayerNotifications", Tag.TAG_COMPOUND);
		for(int i = 0; i < notificationData.size(); ++i)
		{
			CompoundTag tag = notificationData.getCompound(i);
			UUID id = tag.getUUID("Player");
			NotificationData data = NotificationData.loadFrom(tag,lookup);
			if(id != null && data != null)
				this.playerNotifications.put(id, data);
		}
		
	}
	
	private final Map<UUID,NotificationData> playerNotifications = new HashMap<>();
	
	@Nonnull
	@Override
	public CompoundTag save(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		ListTag notificationData = new ListTag();
		this.playerNotifications.forEach((id,data) -> {
			CompoundTag tag = data.save(lookup);
			tag.putUUID("Player", id);
			notificationData.add(tag);
		});
		compound.put("PlayerNotifications", notificationData);
		
		return compound;
	}
	
	private static NotificationSaveData get() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			ServerLevel level = server.getLevel(Level.OVERWORLD);
			if(level != null)
				return level.getDataStorage().computeIfAbsent(new Factory<>(NotificationSaveData::new, NotificationSaveData::new), "lightmanscurrency_notification_data");
		}
		return null;
	}
	
	public static NotificationData GetNotifications(Player player) { return player == null ? new NotificationData() : GetNotifications(player.getUUID()); }
	
	public static NotificationData GetNotifications(UUID playerID) {
		if(playerID == null)
			return new NotificationData();
		NotificationSaveData nsd = get();
		if(nsd != null)
		{
			if(!nsd.playerNotifications.containsKey(playerID))
			{
				nsd.playerNotifications.put(playerID, new NotificationData());
				nsd.setDirty();
			}
			return nsd.playerNotifications.get(playerID);
		}
		return new NotificationData();
	}
	
	public static void MarkNotificationsDirty(UUID playerID) {
		NotificationSaveData nsd = get();
		if(nsd != null)
		{
			nsd.setDirty();
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server != null)
			{
				ServerPlayer player = server.getPlayerList().getPlayer(playerID);
				if(player != null)
					new SPacketSyncNotifications(GetNotifications(playerID)).sendTo(player);
			}
			
		}
	}
	
	public static void PushNotification(UUID playerID, Notification notification) { PushNotification(playerID, notification, true); }
	
	public static void PushNotification(UUID playerID, Notification notification, boolean pushToChat) {
		if(notification == null)
		{
			LightmansCurrency.LogError("Cannot push a null notification!");
			return;
		}
		NotificationData data = GetNotifications(playerID);
		if(data != null)
		{
			//Post event to see if we should send the notification
			NotificationEvent.NotificationSent.Pre event = new NotificationEvent.NotificationSent.Pre(playerID, data, notification);
			if(NeoForge.EVENT_BUS.post(event).isCanceled())
				return;
			//Passed the pre event, add the notification to the notification data
			data.addNotification(event.getNotification());
			//Mark the data as dirty
			MarkNotificationsDirty(playerID);
			//Run the post event to notify anyone who cares that the notification was created.
			NeoForge.EVENT_BUS.post(new NotificationEvent.NotificationSent.Post(playerID, data, event.getNotification()));
			
			//Send the notification message to the client so that it will be posted in chat
			if(pushToChat)
			{
				MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
				if(server != null)
				{
					ServerPlayer player = server.getPlayerList().getPlayer(playerID);
					if(player != null)
						new SPacketChatNotification(notification).sendTo(player);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void OnPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		//Only send their personal notifications
		NotificationData notifications = GetNotifications(event.getEntity());
		new SPacketSyncNotifications(notifications).sendTo(event.getEntity());
	}
	
}
