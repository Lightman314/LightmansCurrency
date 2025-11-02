package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EventCategory extends NotificationCategory {

	public static final NotificationCategoryType<EventCategory> TYPE = new NotificationCategoryType<>(VersionUtil.lcResource("seasonal_event"), EventCategory::getEvent);

	public static final EventCategory INSTANCE = new EventCategory();

	private EventCategory() {}

	@Override
	public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.COINPILE_CHOCOLATE_GOLD); }
	@Override
	public Component getName() { return LCText.NOTIFICATION_SOURCE_EVENT.get(); }
    @Override
	protected NotificationCategoryType<EventCategory> getType() { return TYPE; }
	@Override
	public boolean matches(NotificationCategory other) { return other instanceof EventCategory; }
	@Override
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) { }

	private static EventCategory getEvent(CompoundTag ignored, HolderLookup.Provider lookup) { return INSTANCE; }

}
