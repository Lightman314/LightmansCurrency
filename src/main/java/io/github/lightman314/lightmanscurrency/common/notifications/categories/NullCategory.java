package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class NullCategory extends NotificationCategory {

	public static final NotificationCategoryType<NullCategory> TYPE = new NotificationCategoryType<>(VersionUtil.lcResource("null"), NullCategory::getNull);

	public static final NullCategory INSTANCE = new NullCategory();
	
	private NullCategory() {}
	
	@Nonnull
	@Override
	public IconData getIcon() { return ItemIcon.ofItem(Items.BARRIER); }

	@Nonnull
	@Override
	public MutableComponent getName() { return LCText.NOTIFICATION_SOURCE_NULL.get(); }

	@Nonnull
    @Override
	protected NotificationCategoryType<NullCategory> getType() { return TYPE; }

	@Override
	public boolean matches(NotificationCategory other) { return other instanceof NullCategory; }

	@Override
	protected void saveAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) { }

	private static NullCategory getNull(CompoundTag ignored, @Nonnull HolderLookup.Provider lookup) { return INSTANCE; }
	
}
