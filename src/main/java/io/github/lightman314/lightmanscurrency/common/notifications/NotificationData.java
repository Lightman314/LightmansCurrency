package io.github.lightman314.lightmanscurrency.common.notifications;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification.Category;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class NotificationData {

	List<Notification> notifications = new ArrayList<>();
	public List<Notification> getNotifications() { return this.notifications; }
	public List<Notification> getNotifications(Category category) {
		if(category == Category.GENERAL)
			return this.notifications;
		List<Notification> result = new ArrayList<>();
		for(Notification not : notifications)
		{
			if(category.matches(not.getCategory()))
				result.add(not);
		}
		return result;
	}
	
	public boolean unseenNotification() { return this.unseenNotification(Category.GENERAL); }
	public boolean unseenNotification(Category category) {
		for(Notification n : this.getNotifications(category))
		{
			if(!n.wasSeen())
				return true;
		}
		return false;
	}
	
	public List<Category> getCategories() {
		List<Category> result = new ArrayList<>();
		for(Notification not : this.notifications)
		{
			Category category = not.getCategory();
			if(category != null && result.stream().noneMatch(cat -> cat.matches(category)))
				result.add(category);
		}
		return result;
	}
	
	public void addNotification(Notification newNotification) {
		boolean shouldAdd = true;
		for(int i = 0; i < notifications.size() && shouldAdd; ++i)
		{
			Notification n = notifications.get(i);
			if(n.onNewNotification(newNotification))
			{
				//If new notification was stacked, move it to the top
				this.notifications.remove(n);
				this.notifications.add(0, n);
				shouldAdd = false;
			}
		}
		if(shouldAdd)
			this.notifications.add(0, newNotification);
		int limit = Config.SERVER.notificationLimit.get();
		while(this.notifications.size() > limit)
			this.notifications.remove(this.notifications.get(this.notifications.size() - 1));
	}
	
	public CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		ListTag notificationList = new ListTag();
		for(int i = 0; i < notifications.size(); ++i)
		{
			notificationList.add(notifications.get(i).save());
		}
		compound.put("Notifications", notificationList);
		return compound;
	}
	
	public static NotificationData loadFrom(CompoundTag compound) {
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
				Notification not = Notification.deserialize(notTag);
				if(not != null)
					this.notifications.add(not);
			}
		}
	}
	
}
