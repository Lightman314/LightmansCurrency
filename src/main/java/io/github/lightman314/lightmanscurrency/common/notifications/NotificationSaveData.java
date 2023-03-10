package io.github.lightman314.lightmanscurrency.common.notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.data.MessageUpdateClientNotifications;
import io.github.lightman314.lightmanscurrency.network.message.notifications.MessageClientNotification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class NotificationSaveData extends WorldSavedData {

	private NotificationSaveData() { super("lightmanscurrency_notification_data"); }
	
	public void load(CompoundNBT compound) {
		
		ListNBT notificationData = compound.getList("PlayerNotifications", Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < notificationData.size(); ++i)
		{
			CompoundNBT tag = notificationData.getCompound(i);
			UUID id = tag.getUUID("Player");
			NotificationData data = NotificationData.loadFrom(tag);
			if(id != null && data != null)
				this.playerNotifications.put(id, data);
		}
		
	}
	
	private final Map<UUID,NotificationData> playerNotifications = new HashMap<>();
	
	@Nonnull
	@Override
	public CompoundNBT save(CompoundNBT compound) {
		
		ListNBT notificationData = new ListNBT();
		this.playerNotifications.forEach((id,data) -> {
			CompoundNBT tag = data.save();
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
			ServerWorld level = server.overworld();
			if(level != null)
				return level.getDataStorage().computeIfAbsent(NotificationSaveData::new, "lightmanscurrency_notification_data");
		}
		return null;
	}

	/** @deprecated Use only to transfer notification data from the old Trading Office. */
	@Deprecated
	public static void GiveOldNotificationData(UUID player, NotificationData notifications) {
		NotificationSaveData nsd = get();
		if(nsd != null)
		{
			nsd.playerNotifications.put(player, notifications);
			nsd.setDirty();
		}
	}
	
	public static NotificationData GetNotifications(PlayerEntity player) { return player == null ? new NotificationData() : GetNotifications(player.getUUID()); }
	
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
				ServerPlayerEntity player = server.getPlayerList().getPlayer(playerID);
				if(player != null)
					LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageUpdateClientNotifications(GetNotifications(playerID)));
			}
			
		}
	}
	
	public static boolean PushNotification(UUID playerID, Notification notification) { return PushNotification(playerID, notification, true); }
	
	public static boolean PushNotification(UUID playerID, Notification notification, boolean pushToChat) {
		if(notification == null)
		{
			LightmansCurrency.LogError("Cannot push a null notification!");
			return false;
		}
		NotificationData data = GetNotifications(playerID);
		if(data != null)
		{
			//Post event to see if we should sent the notification
			NotificationEvent.NotificationSent.Pre event = new NotificationEvent.NotificationSent.Pre(playerID, data, notification);
			if(MinecraftForge.EVENT_BUS.post(event))
				return false;
			//Passed the pre event, add the notification to the notification data
			data.addNotification(event.getNotification());
			//Mark the data as dirty
			MarkNotificationsDirty(playerID);
			//Run the post event to notify anyone who cares that the notification was created.
			MinecraftForge.EVENT_BUS.post(new NotificationEvent.NotificationSent.Post(playerID, data, event.getNotification()));
			
			//Send the notification message to the client so that it will be posted in chat
			if(pushToChat)
			{
				MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
				if(server != null)
				{
					ServerPlayerEntity player = server.getPlayerList().getPlayer(playerID);
					if(player != null)
						LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessageClientNotification(notification));
				}
			}
			
			return true;
		}
		return false;
	}
	
	@SubscribeEvent
	public static void OnPlayerLogin(PlayerLoggedInEvent event)
	{
		PacketDistributor.PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getPlayer());
		
		//Only send their personal notifications
		NotificationData notifications = GetNotifications(event.getPlayer());
		LightmansCurrencyPacketHandler.instance.send(target, new MessageUpdateClientNotifications(notifications));
	}

	
	
}