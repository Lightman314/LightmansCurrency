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

public class AddRemoveTradeNotification extends SingleLineNotification {

	public static final NotificationType<AddRemoveTradeNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "add_remove_trade"),AddRemoveTradeNotification::new);
	
	PlayerReference player;
	boolean isAdd;
	int newCount;

	public AddRemoveTradeNotification() {}
	public AddRemoveTradeNotification(PlayerReference player, boolean isAdd, int newCount) { this.player = player; this.isAdd = isAdd; this.newCount = newCount; }
	
	@Nonnull
    @Override
	protected NotificationType<AddRemoveTradeNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Nonnull
	@Override
	public MutableComponent getMessage() {
		return LCText.NOTIFICATION_SETTINGS_ADD_REMOVE_TRADE.get(this.player.getName(true), this.isAdd ? LCText.GUI_ADDED.get() : LCText.GUI_FROM.get(), this.newCount);
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.putBoolean("Add", this.isAdd);
		compound.putInt("NewCount", this.newCount);
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.isAdd = compound.getBoolean("Add");
		this.newCount = compound.getInt("NewCount");
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof AddRemoveTradeNotification n)
		{
			return n.player.is(this.player) && this.isAdd == n.isAdd && this.newCount == n.newCount;
		}
		return false;
	}

}
