package io.github.lightman314.lightmanscurrency.api.notifications.holder;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationStack;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.category.builtin.GeneralCategory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

public final class NotificationHolder implements ISidedContext.Mutable<NotificationHolder>, NotificationConsumer {

    public static final Codec<NotificationHolder> CODEC = NotificationStack.CODEC.listOf().xmap(NotificationHolder::new,NotificationHolder::getNotifications);
    public static final StreamCodec<RegistryFriendlyByteBuf,NotificationHolder> STREAM_CODEC = NotificationStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(NotificationHolder::new,NotificationHolder::getNotifications);

    private ISidedContext context = ISidedContext.LOGICAL_CLIENT;
    @Override
    public NotificationHolder setSidedContext(ISidedContext context) { this.context = context; return this; }

    @Override
    public boolean isClient() { return this.context.isClient(); }

    private final List<NotificationStack> notifications = new ArrayList<>();
    public List<NotificationStack> getNotifications() { return Collections.unmodifiableList(this.notifications); }
    private final Map<NotificationFilter,List<NotificationStack>> filteredCache = new HashMap<>();
    private final List<NotificationCategory> categories = new ArrayList<>();

    public NotificationHolder() {}
    public NotificationHolder(List<NotificationStack> notifications) {
        this.notifications.addAll(notifications);
        this.validateListSize();
        this.updateCategoryCache();
    }

    public void loadFrom(List<NotificationStack> notifications) {
        this.notifications.clear();
        this.notifications.addAll(notifications);
        this.notifications.forEach(n -> n.setSidedContext(this));
        this.validateListSize();
        this.updateCategoryCache();
    }

    private void updateCategoryCache() {
        this.categories.clear();
        for(NotificationStack stack : this.notifications) {
            NotificationCategory category = stack.getCategory();
            if(!this.categories.contains(category))
                this.categories.add(category);
        }
    }

    public List<NotificationStack> getNotifications(NotificationCategory category) {
        if(category == GeneralCategory.INSTANCE)
            return this.getNotifications();
        return this.getNotifications(NotificationFilter.ofCategory(category));
    }
    public List<NotificationStack> getNotifications(NotificationFilter filter) {
        //Cache the results of the query to optimize rendering
        //Cache is cleared when notifications are added or removed
        if(!this.filteredCache.containsKey(filter))
            this.filteredCache.put(filter,this.notifications.stream().filter(filter::filter).toList());
        return this.filteredCache.getOrDefault(filter,List.of());
    }

    public boolean hasUnseenNotification() { return this.hasUnseenNotification(GeneralCategory.INSTANCE); }
    public boolean hasUnseenNotification(NotificationCategory category) { return this.getNotifications(category).stream().anyMatch(NotificationStack::isUnseen); }

    public List<NotificationCategory> getCategories() { return Collections.unmodifiableList(this.categories); }

    @Override
    public void postNotification(Notification notification) {
        if(this.isServer())
            this.postNotificationInternal(notification);
    }

    public void addClientNotification(Notification notification) {
        if(this.isClient())
            this.postNotificationInternal(notification);
    }

    private void postNotificationInternal(Notification notification) {
        boolean shouldAdd = true;
        if(!this.notifications.isEmpty()) {
            NotificationStack mostRecent = this.notifications.getFirst();
            if(mostRecent.tryMergeNotification(notification))
                shouldAdd = false;
        }
        if(shouldAdd) {
            this.notifications.addFirst(new NotificationStack(notification).setSidedContext(this.context));
            //Put the new category at the top of the list as it's the most recent one
            if(!this.categories.contains(notification.getCategory()))
                this.categories.addFirst(notification.getCategory());
            //Clear the cache so that the new notification will be seen in filtered searches
            this.filteredCache.clear();
        }
        this.validateListSize();
    }

    public void deleteNotification(int index) {
        if(index < 0 || index >= this.notifications.size())
            return;
        this.notifications.remove(index);
        //Clear the cache
        this.filteredCache.clear();
        this.updateCategoryCache();
    }
    public void deleteNotification(NotificationCategory category,int index) {
        if(category == GeneralCategory.INSTANCE)
            this.deleteNotification(index);
        else
            this.deleteNotification(NotificationFilter.ofCategory(category),index);
    }
    public int deleteNotification(NotificationFilter filter,int index) {
        for(int i = 0; i < this.notifications.size(); ++i) {
            NotificationStack stack = this.notifications.get(i);
            if(filter.filter(stack)) {
                index--;
                if(index < 0) {
                    //Call the "normal" method to make cache clearing consistent
                    this.deleteNotification(i);
                    return i;
                }
            }
        }
        return -1;
    }

    private void validateListSize() {
        int limit = LCConfig.SERVER.notificationLimit.get();
        if(this.notifications.size() > limit) {
            //Remove the excess notifications
            while(this.notifications.size() > limit)
                this.notifications.removeLast();
            //Clear/update the caches
            this.filteredCache.clear();
            this.updateCategoryCache();
        }
    }

}