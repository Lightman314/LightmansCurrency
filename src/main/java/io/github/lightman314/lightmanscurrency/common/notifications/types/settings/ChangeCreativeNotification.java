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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ChangeCreativeNotification extends SingleLineNotification {

	public static final NotificationType<ChangeCreativeNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("change_creative"),ChangeCreativeNotification::new);
	
	PlayerReference player;
	boolean creative;

	private ChangeCreativeNotification() {}
	public ChangeCreativeNotification(PlayerReference player, boolean creative) { this.player = player; this.creative = creative; }
	
    @Override
	protected NotificationType<ChangeCreativeNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public Component getMessage() {
		return LCText.NOTIFICATION_SETTINGS_CHANGE_CREATIVE.get(this.player.getName(true), this.creative ? LCText.NOTIFICATION_SETTINGS_CHANGE_CREATIVE_ENABLED.get() : LCText.NOTIFICATION_SETTINGS_CHANGE_CREATIVE_DISABLED.get());
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		compound.put("Player", this.player.save());
		compound.putBoolean("Creative", this.creative);
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.creative = compound.getBoolean("Creative");
	}
	
	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ChangeCreativeNotification n)
		{
			return n.player.is(this.player) && n.creative == this.creative;
		}
		return false;
	}
	
}
