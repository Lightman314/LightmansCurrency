package io.github.lightman314.lightmanscurrency.api.notifications;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;

public class NotificationData implements IClientTracker {

	private boolean isClient = false;
	@Override
	public boolean isClient() { return this.isClient; }

	List<Notification> notifications = new ArrayList<>();
	public List<Notification> getNotifications() { return this.notifications; }
	public List<Notification> getNotifications(@Nonnull NotificationCategory category) {
		if(category == NotificationCategory.GENERAL)
			return this.notifications;
		List<Notification> result = new ArrayList<>();
		for(Notification not : this.notifications)
		{
			if(category.matches(not.getCategory()))
				result.add(not);
		}
		return result;
	}
	
	public boolean unseenNotification() { return this.unseenNotification(NotificationCategory.GENERAL); }
	public boolean unseenNotification(@Nonnull NotificationCategory category) {
		for(Notification n : this.getNotifications(category))
		{
			if(!n.wasSeen())
				return true;
		}
		return false;
	}

	@Nonnull
	public List<NotificationCategory> getCategories() {
		List<NotificationCategory> result = new ArrayList<>();
		for(Notification not : this.notifications)
		{
			NotificationCategory category = not.getCategory();
			if(category != null && result.stream().noneMatch(cat -> cat.matches(category)))
				result.add(category);
		}
		return result;
	}
	
	public void addNotification(@Nonnull Notification newNotification) {
		boolean shouldAdd = true;
		if(!this.notifications.isEmpty())
		{
			Notification mostRecent = this.notifications.getFirst();
			if(mostRecent.onNewNotification(newNotification))
				shouldAdd = false;
		}
		if(shouldAdd)
			this.notifications.addFirst(newNotification);
		
		this.validateListSize();
		
	}

	public void deleteNotification(int notificationIndex)
	{
		if(notificationIndex < 0 || notificationIndex >= this.notifications.size())
			return;
		this.notifications.remove(notificationIndex);
	}

	public void deleteNotification(@Nonnull NotificationCategory category,int notificationIndex)
	{
		if(category == NotificationCategory.GENERAL)
		{
			this.deleteNotification(notificationIndex);
			return;
		}
		for(int i = 0; i < this.notifications.size(); ++i)
		{
			Notification n = this.notifications.get(i);
			if(category.matches(n.getCategory()))
			{
				notificationIndex--;
				if(notificationIndex < 0)
				{
					this.notifications.remove(i);
					return;
				}
			}
		}
	}
	
	private void validateListSize()
	{
		int limit = LCConfig.SERVER.notificationLimit.get();
		while(this.notifications.size() > limit)
			this.notifications.removeLast();
	}

	@Nonnull
	public CompoundTag save(@Nonnull HolderLookup.Provider lookup) {
		CompoundTag compound = new CompoundTag();
		ListTag notificationList = new ListTag();
		for (Notification notification : new ArrayList<>(this.notifications))
			notificationList.add(notification.save(lookup));
		compound.put("Notifications", notificationList);
		return compound;
	}

	@Nonnull
	public static NotificationData loadFrom(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		NotificationData data = new NotificationData();
		data.load(compound, lookup);
		return data;
	}
	
	public void load(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		if(compound.contains("Notifications", Tag.TAG_LIST))
		{
			this.notifications = new ArrayList<>();
			ListTag notificationList = compound.getList("Notifications", Tag.TAG_COMPOUND);
			for(int i = 0; i < notificationList.size(); ++i)
			{
				CompoundTag notTag = notificationList.getCompound(i);
				Notification not = NotificationAPI.API.LoadNotification(notTag, lookup);
				if(not != null)
					this.notifications.add(not);
			}
			this.validateListSize();
			if(this.isClient)
				this.flagAsClient();
		}
	}

	public final void flagAsClient()
	{
		this.isClient = true;
		for(Notification n : this.notifications)
			n.flagAsClient();
	}
	
}
