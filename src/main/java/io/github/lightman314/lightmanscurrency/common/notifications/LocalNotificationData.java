package io.github.lightman314.lightmanscurrency.common.notifications;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class LocalNotificationData {

	private final List<Notification> notifications = new ArrayList<>();
	public List<Notification> getNotifications() { return new ArrayList<>(this.notifications); }
	
	public ListTag save()
	{
		ListTag notificationList = new ListTag();
		for(Notification n : this.notifications)
			notificationList.add(n.save());
		return notificationList;
	}
	
	public void load(ListTag list)
	{
		this.notifications.clear();
		for(int i = 0; i < list.size(); ++i)
		{
			CompoundTag tag = list.getCompound(i);
			Notification n = Notification.deserialize(tag);
			if(n != null)
				this.notifications.add(n);
		}
	}
	
	public void addNotification(Notification notification)
	{
		int mergedIndex = -1;
		for(int i = 0; mergedIndex < 0 && i < this.notifications.size(); ++i)
		{
			if(this.notifications.get(i).canMerge(notification))
			{
				mergedIndex = i;
			}
		}
	}
	
}
