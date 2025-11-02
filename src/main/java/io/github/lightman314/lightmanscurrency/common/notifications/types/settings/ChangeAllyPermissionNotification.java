package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ChangeAllyPermissionNotification extends SingleLineNotification {

	public static final NotificationType<ChangeAllyPermissionNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("change_ally_permissions"),ChangeAllyPermissionNotification::new);
	
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

    @Override
	protected NotificationType<ChangeAllyPermissionNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public Component getMessage() {
		if(this.oldValue == 0)
			return LCText.NOTIFICATION_SETTINGS_CHANGE_ALLY_PERMISSIONS_SIMPLE.get(this.player.getName(true), this.permission, this.newValue);
		else
			return LCText.NOTIFICATION_SETTINGS_CHANGE_ALLY_PERMISSIONS.get(this.player.getName(true), this.permission, this.oldValue, this.newValue);
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.putString("Permission", this.permission);
		compound.putInt("NewValue", this.newValue);
		compound.putInt("OldValue", this.oldValue);
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.permission = compound.getString("Permission");
		this.newValue = compound.getInt("NewValue");
		this.oldValue = compound.getInt("OldValue");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ChangeAllyPermissionNotification n)
		{
			return n.player.is(this.player) && n.permission.equals(this.permission) && n.newValue == this.newValue && n.oldValue == this.oldValue;
		}
		return false;
	}
	
	
}
