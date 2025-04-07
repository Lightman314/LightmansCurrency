package io.github.lightman314.lightmanscurrency.api.notifications;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public abstract class NotificationCategory implements ITab
{
	
	public static final NotificationCategoryType<?> GENERAL_TYPE = new NotificationCategoryType<>(VersionUtil.lcResource( "general"), NotificationCategory::getGeneral);

	@Nonnull
	public final MutableComponent getTooltip() { return this.getName(); }
	@Nonnull
	public abstract MutableComponent getName();
	@Nonnull
	protected abstract NotificationCategoryType<?> getType();
	
	public abstract boolean matches(NotificationCategory other);

	private static NotificationCategory getGeneral(CompoundTag _tag,HolderLookup.Provider _lookup) { return GENERAL; }

	public static final NotificationCategory GENERAL = new NotificationCategory() {
		@Nonnull
		@Override
		public IconData getIcon() { return IconData.of(Items.CHEST); }
		@Nonnull
		@Override
		public MutableComponent getName() { return LCText.NOTIFICATION_SOURCE_GENERAL.get(); }
		@Override
		public boolean matches(NotificationCategory other) { return other == GENERAL; }
		@Nonnull
		@Override
		protected NotificationCategoryType<?> getType() { return GENERAL_TYPE; }
		@Override
		protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {}
	};
	
	public final CompoundTag save(@Nonnull HolderLookup.Provider lookup) {
		CompoundTag compound = new CompoundTag();
		compound.putString("type", this.getType().toString());
		this.saveAdditional(compound,lookup);
		return compound;
	}
	
	protected abstract void saveAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup);

	public final boolean notGeneral() { return this != GENERAL; }
	
}
