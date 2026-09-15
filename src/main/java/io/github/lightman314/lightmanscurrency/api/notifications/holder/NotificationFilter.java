package io.github.lightman314.lightmanscurrency.api.notifications.holder;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationStack;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;

import java.util.Objects;

public abstract class NotificationFilter {

    public abstract boolean filter(Notification notification);
    public final boolean filter(NotificationStack stack) { return this.filter(stack.getNotification()); }

    public abstract int hashCode();
    protected abstract boolean equals(NotificationFilter other);
    public final boolean equals(Object other) {
        if(other == this)
            return true;
        if(other instanceof NotificationFilter filter)
            return this.equals(filter);
        return false;
    }

    public NotificationFilter inverted() { return new InvertedFilter(this); }

    public static NotificationFilter ofCategory(NotificationCategory category) { return new CategoryFilter(category); }

    private static class CategoryFilter extends NotificationFilter {
        private final NotificationCategory category;
        private CategoryFilter(NotificationCategory category) { this.category = category; }
        @Override
        public boolean filter(Notification notification) { return notification.getCategory().equals(this.category); }
        @Override
        public int hashCode() { return this.category.hashCode(); }
        @Override
        protected boolean equals(NotificationFilter other) {
            return other instanceof CategoryFilter f && f.category.equals(this.category);
        }
    }
    private static class InvertedFilter extends NotificationFilter {
        private final NotificationFilter filter;
        private InvertedFilter(NotificationFilter filter) { this.filter = filter; }
        @Override
        public boolean filter(Notification notification) { return !this.filter.filter(notification); }
        @Override
        public int hashCode() { return Objects.hash(16,this.filter.hashCode()); }
        @Override
        protected boolean equals(NotificationFilter other) { return other instanceof InvertedFilter f && f.filter.equals(this.filter); }
        @Override
        public NotificationFilter inverted() { return this.filter; }
    }

}