package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import net.minecraft.network.chat.Component;

public class EventCategory extends NotificationCategory {

    public static final EventCategory INSTANCE = new EventCategory();

	public static final NotificationCategoryType<EventCategory> TYPE = new NotificationCategoryType.Instance<>(INSTANCE);

	private EventCategory() {}

	@Override
	public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.COINPILE_CHOCOLATE_GOLD); }
	@Override
	public Component getName() { return LCText.NOTIFICATION_SOURCE_EVENT.get(); }
    @Override
	protected NotificationCategoryType<EventCategory> getType() { return TYPE; }
	@Override
	public boolean matches(NotificationCategory other) { return other instanceof EventCategory; }

}
