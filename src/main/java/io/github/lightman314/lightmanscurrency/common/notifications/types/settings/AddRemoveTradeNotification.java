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
public class AddRemoveTradeNotification extends SingleLineNotification {

	public static final NotificationType<AddRemoveTradeNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("add_remove_trade"),AddRemoveTradeNotification::new);
	
	PlayerReference player;
	boolean isAdd;
	int newCount;

	public AddRemoveTradeNotification() {}
	public AddRemoveTradeNotification(PlayerReference player, boolean isAdd, int newCount) { this.player = player; this.isAdd = isAdd; this.newCount = newCount; }

    @Override
	protected NotificationType<AddRemoveTradeNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public Component getMessage() {
		return LCText.NOTIFICATION_SETTINGS_ADD_REMOVE_TRADE.get(this.player.getName(true), this.isAdd ? LCText.GUI_ADDED.get() : LCText.GUI_FROM.get(), this.newCount);
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.putBoolean("Add", this.isAdd);
		compound.putInt("NewCount", this.newCount);
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.isAdd = compound.getBoolean("Add");
		this.newCount = compound.getInt("NewCount");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof AddRemoveTradeNotification n)
		{
			return n.player.is(this.player) && this.isAdd == n.isAdd && this.newCount == n.newCount;
		}
		return false;
	}

}
