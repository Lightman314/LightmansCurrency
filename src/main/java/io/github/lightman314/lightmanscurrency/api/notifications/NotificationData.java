package io.github.lightman314.lightmanscurrency.api.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;

public class NotificationData implements ISidedObject {

	private boolean isClient = false;
	@Override
	public boolean isClient() { return this.isClient; }

	List<Notification> notifications = new ArrayList<>();
	public List<Notification> getNotifications() { return this.notifications; }
	public List<Notification> getNotifications(@Nonnull NotificationCategory category) {
		if(category == NotificationCategory.GENERAL)
			return this.notifications;
		return this.getNotifications(n -> n.getCategory().matches(category));
	}
	public List<Notification> getNotifications(Predicate<Notification> filter) {
		List<Notification> result = new ArrayList<>();
		for(Notification not : this.notifications)
		{
			if(filter.test(not))
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
			Notification mostRecent = this.notifications.get(0);
			if(mostRecent.onNewNotification(newNotification))
				shouldAdd = false;
		}
		if(shouldAdd)
			this.notifications.add(0, newNotification);
		
		this.validateListSize();
		
	}

	public void deleteNotification(int notificationIndex)
	{
		if(notificationIndex < 0 || notificationIndex >= this.notifications.size())
			return;
		this.notifications.remove(notificationIndex);
	}

	public void deleteNotification(@Nonnull NotificationCategory category, int notificationIndex)
	{
		if(category == NotificationCategory.GENERAL)
		{
			this.deleteNotification(notificationIndex);
			return;
		}
		this.deleteNotification(n -> n.getCategory().matches(category),notificationIndex);
	}
	public void deleteNotification(Predicate<Notification> filter, int notificationIndex) {
		for(int i = 0; i < this.notifications.size(); ++i)
		{
			Notification n = this.notifications.get(i);
			if(filter.test(n))
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
			this.notifications.remove(this.notifications.size() - 1);
	}

	@Nonnull
	public CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		ListTag notificationList = new ListTag();
		for (Notification notification : new ArrayList<>(this.notifications))
			notificationList.add(notification.save());
		compound.put("Notifications", notificationList);
		return compound;
	}

	@Nonnull
	public static NotificationData loadFrom(@Nonnull CompoundTag compound) {
		NotificationData data = new NotificationData();
		data.load(compound);
		return data;
	}
	
	public void load(CompoundTag compound) {
		if(compound.contains("Notifications", Tag.TAG_LIST))
		{
			this.notifications = new ArrayList<>();
			ListTag notificationList = compound.getList("Notifications", Tag.TAG_COMPOUND);
			for(int i = 0; i < notificationList.size(); ++i)
			{
				CompoundTag notTag = notificationList.getCompound(i);
				Notification not = NotificationAPI.API.LoadNotification(notTag);
				if(not != null)
					this.notifications.add(not);
			}
			this.validateListSize();
			if(this.isClient)
				this.flagAsClient();
		}
	}

	@Override
	@Nonnull
	public final NotificationData flagAsClient() { return this.flagAsClient(true); }
	@Override
	@Nonnull
	public final NotificationData flagAsClient(boolean isClient) {
		this.isClient = isClient;
		for(Notification n : this.notifications)
			n.flagAsClient(this);
		return this;
	}
	@Override
	@Nonnull
	public final NotificationData flagAsClient(IClientTracker context) { return this.flagAsClient(context.isClient()); }
	
}
