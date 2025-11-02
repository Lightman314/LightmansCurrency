package io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications;

import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

public class NotificationTabButton extends TabButton {

	final Supplier<NotificationData> dataSource;
	final NotificationCategory category;

	private NotificationTabButton(Builder builder) {
		super(TabButton.builder().copyFrom(builder).tab(builder.category).rotation(builder.rotation));
		this.category = builder.category;
		this.dataSource = builder.data;
	}


	protected boolean unseenNotifications() { return this.dataSource.get().unseenNotification(this.category); }

	@Override
	protected Function<WidgetRotation,FixedSizeSprite> getSprite() { return this.unseenNotifications() ? YELLOW : NORMAL; }

	@Nonnull
	public static Builder nBuilder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyButtonBuilder<Builder>
	{
		private Builder() { super(25,25); }
		@Override
		protected Builder getSelf() { return this; }

		private Supplier<NotificationData> data = () -> null;
		private NotificationCategory category = null;
		private WidgetRotation rotation = WidgetRotation.TOP;

		public Builder data(Supplier<NotificationData> data) { this.data = data; return this; }
		public Builder category(NotificationCategory category) { this.category = category; return this; }
		public Builder rotation(WidgetRotation rotation) { this.rotation = rotation; return this; }

		public NotificationTabButton build() { return new NotificationTabButton(this); }

	}

}