package io.github.lightman314.lightmanscurrency.api.notifications;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class NotificationCategory implements ITab
{
	
	public static final NotificationCategoryType<?> GENERAL_TYPE = new NotificationCategoryType<>(VersionUtil.lcResource("general"), NotificationCategory::getGeneral);

	public final Component getTooltip() { return this.getName(); }
	public abstract Component getName();
	
	protected abstract NotificationCategoryType<?> getType();
	
	public abstract boolean matches(NotificationCategory other);

	private static NotificationCategory getGeneral(CompoundTag ignored) { return GENERAL; }

	public static final NotificationCategory GENERAL = new NotificationCategory() {
		
		@Override
		public IconData getIcon() { return ItemIcon.ofItem(Items.CHEST); }
		
		@Override
		public Component getName() { return LCText.NOTIFICATION_SOURCE_GENERAL.get(); }
		@Override
		public boolean matches(NotificationCategory other) { return other == GENERAL; }
		
		@Override
		protected NotificationCategoryType<?> getType() { return GENERAL_TYPE; }
		@Override
		protected void saveAdditional(CompoundTag compound) {}
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
