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
public class ChangeNameNotification extends SingleLineNotification {

	public static final NotificationType<ChangeNameNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("changed_name"),ChangeNameNotification::new);

	private PlayerReference player;
	private String oldName;
	private String newName;

	private ChangeNameNotification() {}
	public ChangeNameNotification(PlayerReference player, String newName, String oldName) { this.player = player; this.newName = newName; this.oldName = oldName; }

    @Override
	protected NotificationType<ChangeNameNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public Component getMessage() {
		if(this.oldName.isBlank())
			return LCText.NOTIFICATION_SETTINGS_CHANGE_NAME_SET.get(this.player.getName(true), this.newName);
		else if(this.newName.isBlank())
			return LCText.NOTIFICATION_SETTINGS_CHANGE_NAME_RESET.get(this.player.getName(true), this.oldName);
		else
			return LCText.NOTIFICATION_SETTINGS_CHANGE_NAME.get(this.player.getName(true), this.oldName, this.newName);
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.putString("OldName", this.oldName);
		compound.putString("NewName", this.newName);
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.oldName = compound.getString("OldName");
		this.newName = compound.getString("NewName");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ChangeNameNotification n)
		{
			return n.player.is(this.player) && n.newName.equals(this.newName) && n.oldName.equals(this.oldName);
		}
		return false;
	}
	
}
