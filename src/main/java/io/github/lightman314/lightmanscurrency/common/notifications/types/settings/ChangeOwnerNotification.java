package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ChangeOwnerNotification extends Notification {

	public static final NotificationType<ChangeOwnerNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "change_ownership"),ChangeOwnerNotification::new);
	
	PlayerReference player;
	Owner newOwner;
	Owner oldOwner;
	
	private ChangeOwnerNotification() { }

	public ChangeOwnerNotification(PlayerReference player, Owner newOwner, Owner oldOwner) {
		this.player = player;
		this.newOwner = newOwner;
		this.oldOwner = oldOwner;
	}
	
	@Nonnull
    @Override
	protected NotificationType<ChangeOwnerNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Nonnull
	@Override
	public MutableComponent getMessage() {
		if(this.newOwner.asPlayerReference().isExact(this.player))
			return LCText.NOTIFICATION_SETTINGS_CHANGE_OWNER_TAKEN.get(this.newOwner.getName(), this.oldOwner.getName());
		if(this.oldOwner.asPlayerReference().isExact(this.player))
			return LCText.NOTIFICATION_SETTINGS_CHANGE_OWNER_PASSED.get(this.oldOwner.getName(), this.newOwner.getName());
		else
			return LCText.NOTIFICATION_SETTINGS_CHANGE_OWNER_TRANSFERRED.get(this.player.getName(true), this.oldOwner.getName(), this.newOwner.getName());
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.put("NewOwner", this.newOwner.save());
		compound.put("OldOwner", this.oldOwner.save());
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.newOwner = Owner.load(compound.getCompound("NewOwner"));
		this.oldOwner = Owner.load(compound.getCompound("OldOwner"));
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof ChangeOwnerNotification n)
		{
			return n.player.is(this.player) && n.newOwner.matches(this.newOwner) && n.oldOwner.matches(this.oldOwner);
		}
		return false;
	}


	
	
	
}
