package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ChangeAllyPermissionNotification extends SingleLineNotification {

	public static final NotificationType<ChangeAllyPermissionNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID,"change_ally_permissions"),ChangeAllyPermissionNotification::new);
	
	PlayerReference player;
	String permission;
	int newValue;
	int oldValue;

	private ChangeAllyPermissionNotification() {}

	public ChangeAllyPermissionNotification(PlayerReference player, String permission, int newValue, int oldValue) {
		this.player = player;
		this.permission = permission;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	@Nonnull
    @Override
	protected NotificationType<ChangeAllyPermissionNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Nonnull
	@Override
	public MutableComponent getMessage() {
		if(this.oldValue == 0)
			return LCText.NOTIFICATION_SETTINGS_CHANGE_ALLY_PERMISSIONS_SIMPLE.get(this.player.getName(true), this.permission, this.newValue);
		else
			return LCText.NOTIFICATION_SETTINGS_CHANGE_ALLY_PERMISSIONS.get(this.player.getName(true), this.permission, this.oldValue, this.newValue);
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.putString("Permission", this.permission);
		compound.putInt("NewValue", this.newValue);
		compound.putInt("OldValue", this.oldValue);
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.permission = compound.getString("Permission");
		this.newValue = compound.getInt("NewValue");
		this.oldValue = compound.getInt("OldValue");
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof ChangeAllyPermissionNotification n)
		{
			return n.player.is(this.player) && n.permission.equals(this.permission) && n.newValue == this.newValue && n.oldValue == this.oldValue;
		}
		return false;
	}
	
	
}
