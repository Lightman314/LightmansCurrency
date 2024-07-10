package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.TeamOwner;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ChangeOwnerNotification extends Notification {

	public static final NotificationType<ChangeOwnerNotification> TYPE = new NotificationType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "change_ownership"),ChangeOwnerNotification::new);
	
	PlayerReference player;
	Owner newOwner;
	Owner oldOwner;
	
	private ChangeOwnerNotification() { }

	public ChangeOwnerNotification(@Nonnull PlayerReference player, @Nonnull Owner newOwner, @Nonnull Owner oldOwner) {
		this.player = player;
		this.newOwner = newOwner.copy();
		this.newOwner.setParent(this);
		this.oldOwner = oldOwner.copy();
		this.oldOwner.setParent(this);
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
	protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		compound.put("Player", this.player.save());
		compound.put("NewOwner", this.newOwner.save(lookup));
		compound.put("OldOwner", this.oldOwner.save(lookup));
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.newOwner = safeLoad(compound.getCompound("NewOwner"),lookup);
		this.newOwner.setParent(this);
		this.oldOwner = safeLoad(compound.getCompound("OldOwner"),lookup);
		this.oldOwner.setParent(this);
	}

	@Nonnull
	private static Owner safeLoad(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup)
	{
		if(tag.contains("Type"))
		{
			Owner o = Owner.load(tag,lookup);
			return o != null ? o : Owner.getNull();
		}
		if(tag.contains("Player"))
		{
			PlayerReference pr = PlayerReference.load(tag.getCompound("Player"));
			if(pr != null)
				return PlayerOwner.of(pr);
		}
		if(tag.contains("Team"))
		{
			long teamID = tag.getLong("Team");
			return TeamOwner.of(teamID);
		}
		return Owner.getNull();
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
