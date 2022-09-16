package io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.minecraft.client.gui.Font;
import net.minecraftforge.common.util.NonNullSupplier;

public class NotificationTabButton extends TabButton {

	final NonNullSupplier<NotificationData> dataSource;
	final NotificationCategory category;
	
	public NotificationTabButton(OnPress pressable, Font font, NonNullSupplier<NotificationData> dataSource, NotificationCategory category) {
		super(pressable, font, category);
		this.category = category;
		this.dataSource = dataSource;
	}
	
	protected boolean unseenNotifications() { return this.dataSource.get().unseenNotification(this.category); }
	
	@Override
	protected int getColor() { return this.unseenNotifications() ? 0xFFFF00 : this.tab.getColor(); }

}
