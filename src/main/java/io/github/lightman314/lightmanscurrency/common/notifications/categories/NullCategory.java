package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class NullCategory extends NotificationCategory {

	public static final NotificationCategoryType<NullCategory> TYPE = new NotificationCategoryType<>(new ResourceLocation(LightmansCurrency.MODID, "null"), NullCategory::getNull);

	public static final NullCategory INSTANCE = new NullCategory();
	
	private NullCategory() {}
	
	@Nonnull
	@Override
	public @NotNull IconData getIcon() { return IconData.of(Items.BARRIER); }

	@Nonnull
	@Override
	public MutableComponent getName() { return Component.literal("NULL"); }

	@Nonnull
    @Override
	protected NotificationCategoryType<NullCategory> getType() { return TYPE; }

	@Override
	public boolean matches(NotificationCategory other) { return other instanceof NullCategory; }

	@Override
	protected void saveAdditional(CompoundTag compound) { }

	private static NullCategory getNull(CompoundTag ignored) { return INSTANCE; }
	
}
