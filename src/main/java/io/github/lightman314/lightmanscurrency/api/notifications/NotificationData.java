package io.github.lightman314.lightmanscurrency.api.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;

public class NotificationData implements ISidedObject {

    public static final Codec<NotificationData> CODEC = Codec.withAlternative(
            Notification.CODEC.listOf().xmap(NotificationData::new,NotificationData::getNotifications),
            CodecHelper.oldValueLoader(NotificationData::loadOldData,"Notification Data"));
    public static final StreamCodec<RegistryFriendlyByteBuf,NotificationData> STREAM_CODEC = Notification.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(NotificationData::new,NotificationData::getNotifications);

	private boolean isClient = false;
	@Override
	public boolean isClient() { return this.isClient; }

    public NotificationData() {}
    public NotificationData(List<Notification> notifications) {
        this.notifications.addAll(notifications);
    }

    public void copyFrom(NotificationData data)
    {
        this.notifications.addAll(data.notifications);
        for(Notification n : this.notifications)
            n.flagAsClient(this);
    }

	List<Notification> notifications = new ArrayList<>();
	public List<Notification> getNotifications() { return this.notifications; }

	public List<Notification> getNotifications(NotificationCategory category) {
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
	public boolean unseenNotification(NotificationCategory category) {
		for(Notification n : this.getNotifications(category))
		{
			if(!n.wasSeen())
				return true;
		}
		return false;
	}
	
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
	
	public void addNotification(Notification newNotification) {
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
	public void deleteNotification(NotificationCategory category,int notificationIndex)
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
			this.notifications.removeLast();
	}

    @Deprecated
	private static NotificationData loadOldData(CompoundTag compound, HolderLookup.Provider lookup) {
		NotificationData data = new NotificationData();
        if(compound.contains("Notifications",Tag.TAG_LIST))
        {
            data.notifications = new ArrayList<>(Notification.LIST_CODEC.decode(RegistryOps.create(NbtOps.INSTANCE,lookup),compound.get("Notifications")).getOrThrow().getFirst());
            data.validateListSize();
        }
		return data;
	}

	@Override
	public final NotificationData flagAsClient() { return this.flagAsClient(true); }
	@Override
	public final NotificationData flagAsClient(boolean isClient) {
		this.isClient = isClient;
		for(Notification n : this.notifications)
			n.flagAsClient(this);
		return this;
	}
	@Override
	public final NotificationData flagAsClient(IClientTracker context) { return this.flagAsClient(context.isClient()); }

}
