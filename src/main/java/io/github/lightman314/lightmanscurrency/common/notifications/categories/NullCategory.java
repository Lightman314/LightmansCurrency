package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class NullCategory extends NotificationCategory {

    public static final NullCategory INSTANCE = new NullCategory();

	public static final NotificationCategoryType<NullCategory> TYPE = new NotificationCategoryType.Instance<>(INSTANCE);
	
	private NullCategory() {}

	@Override
	public IconData getIcon() { return ItemIcon.ofItem(Items.BARRIER); }

	@Override
	public Component getName() { return LCText.NOTIFICATION_SOURCE_NULL.get(); }

    @Override
	protected NotificationCategoryType<NullCategory> getType() { return TYPE; }

	@Override
	public boolean matches(NotificationCategory other) { return other instanceof NullCategory; }

}
