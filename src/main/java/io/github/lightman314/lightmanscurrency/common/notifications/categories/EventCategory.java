package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class EventCategory extends NotificationCategory {

    public static final NotificationCategoryType<EventCategory> TYPE = new NotificationCategoryType<>(VersionUtil.lcResource("seasonal_event"), EventCategory::getEvent);

    public static final EventCategory INSTANCE = new EventCategory();

    private EventCategory() {}

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(ModBlocks.COINPILE_CHOCOLATE_GOLD); }
    @Nonnull
    @Override
    public MutableComponent getName() { return LCText.NOTIFICATION_SOURCE_EVENT.get(); }
    @Nonnull
    @Override
    protected NotificationCategoryType<EventCategory> getType() { return TYPE; }
    @Override
    public boolean matches(NotificationCategory other) { return other instanceof EventCategory; }
    @Override
    protected void saveAdditional(CompoundTag compound) { }

    private static EventCategory getEvent(CompoundTag ignored) { return INSTANCE; }

}