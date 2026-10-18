package io.github.lightman314.lightmanscurrency.api.notifications.category.builtin;

import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.notifications.category.SingletonNotificationCategory;
import net.minecraft.world.item.Items;

public final class UnfilteredCategory extends SingletonNotificationCategory {

    public static final UnfilteredCategory INSTANCE = new UnfilteredCategory();
    public static final NotificationCategoryType<UnfilteredCategory> TYPE = SingletonNotificationCategory.buildType(INSTANCE);

    private UnfilteredCategory() {}
    @Override
    public IconData getIcon() { return ItemIcon.of(Items.CHEST); }
    @Override
    public NotificationCategoryType<?> getType() { return TYPE; }

}
