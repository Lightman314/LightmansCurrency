package io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NotificationTabButton extends TabButton {

	final Supplier<NotificationData> dataSource;
	final NotificationCategory category;
	
	public NotificationTabButton(Consumer<EasyButton> pressable, Supplier<NotificationData> dataSource, NotificationCategory category) {
		super(pressable, category);
		this.category = category;
		this.dataSource = dataSource;
	}
	
	protected boolean unseenNotifications() { return this.dataSource.get().unseenNotification(this.category); }
	
	@Override
	protected int getColor() { return this.unseenNotifications() ? 0xFFFF00 : this.tab.getColor(); }

}
