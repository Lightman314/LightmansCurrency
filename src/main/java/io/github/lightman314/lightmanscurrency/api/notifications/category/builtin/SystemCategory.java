package io.github.lightman314.lightmanscurrency.api.notifications.category.builtin;

import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.notifications.category.SingletonNotificationCategory;
import net.minecraft.world.item.Items;

public final class SystemCategory extends SingletonNotificationCategory {

    public static final SystemCategory INSTANCE = new SystemCategory();
    public static final NotificationCategoryType<SystemCategory> TYPE = SingletonNotificationCategory.buildType(INSTANCE);

    private SystemCategory() {}
    @Override
    public IconData getIcon() { return ItemIcon.of(Items.COMMAND_BLOCK); }
    @Override
    public NotificationCategoryType<?> getType() { return TYPE; }

}
