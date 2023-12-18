package io.github.lightman314.lightmanscurrency.api.notifications;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public abstract class NotificationCategory implements ITab
{
	
	public static final NotificationCategoryType<?> GENERAL_TYPE = new NotificationCategoryType<>(new ResourceLocation(LightmansCurrency.MODID, "general"), NotificationCategory::getGeneral);

	@Nonnull
	public final MutableComponent getTooltip() { return this.getName(); }
	@Nonnull
	public abstract MutableComponent getName();
	public final int getColor() { return 0xFFFFFF; }
	@Nonnull
	protected abstract NotificationCategoryType<?> getType();
	
	public abstract boolean matches(NotificationCategory other);

	private static NotificationCategory getGeneral(CompoundTag ignored) { return GENERAL; }

	public static final NotificationCategory GENERAL = new NotificationCategory() {
		@Nonnull
		@Override
		public IconData getIcon() { return IconData.of(Items.CHEST); }
		@Nonnull
		@Override
		public MutableComponent getName() { return EasyText.translatable("notifications.source.general"); }
		@Override
		public boolean matches(NotificationCategory other) { return other == GENERAL; }
		@Nonnull
		@Override
		protected NotificationCategoryType<?> getType() { return GENERAL_TYPE; }
		@Override
		protected void saveAdditional(@Nonnull CompoundTag compound) {}
	};
	
	public final CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		compound.putString("type", this.getType().toString());
		this.saveAdditional(compound);
		return compound;
	}
	
	protected abstract void saveAdditional(CompoundTag compound);

	public final boolean notGeneral() { return this != GENERAL; }
	
}
