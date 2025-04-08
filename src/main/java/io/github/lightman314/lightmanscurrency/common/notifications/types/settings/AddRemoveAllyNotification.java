package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class AddRemoveAllyNotification extends SingleLineNotification {

	public static final NotificationType<AddRemoveAllyNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("add_remove_ally"),AddRemoveAllyNotification::new);
	
	PlayerReference player;
	boolean isAdd;
	PlayerReference ally;

	private AddRemoveAllyNotification() {}

	public AddRemoveAllyNotification(PlayerReference player, boolean isAdd, PlayerReference ally) {
		this.player = player;
		this.isAdd = isAdd;
		this.ally = ally;
	}
	
	@Nonnull
    @Override
	protected NotificationType<AddRemoveAllyNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Nonnull
	@Override
	public MutableComponent getMessage() {
		return LCText.NOTIFICATION_SETTINGS_ADD_REMOVE_ALLY.get(this.player.getName(true), this.isAdd ? LCText.GUI_ADDED.get() : LCText.GUI_REMOVED.get(), this.ally.getName(true), this.isAdd ? LCText.GUI_TO.get() : LCText.GUI_FROM.get());
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.putBoolean("Add", this.isAdd);
		compound.put("Ally", this.ally.save());
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.isAdd = compound.getBoolean("Add");
		this.ally = PlayerReference.load(compound.getCompound("Ally"));
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof AddRemoveAllyNotification n)
		{
			return n.player.is(this.player) && n.isAdd == this.isAdd && n.ally.is(this.ally);
		}
		return false;
	}

}
