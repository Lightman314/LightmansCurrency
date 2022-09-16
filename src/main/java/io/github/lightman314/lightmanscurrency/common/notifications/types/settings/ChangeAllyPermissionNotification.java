package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class ChangeAllyPermissionNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"change_ally_permissions");
	
	PlayerReference player;
	String permission;
	int newValue;
	int oldValue;
	
	public ChangeAllyPermissionNotification(PlayerReference player, String permission, int newValue, int oldValue) {
		this.player = player;
		this.permission = permission;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}
	
	public ChangeAllyPermissionNotification(CompoundTag compound) { this.load(compound); }

	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public MutableComponent getMessage() {
		if(this.oldValue == 0)
			return Component.translatable("log.settings.permission.ally.simple", this.player.lastKnownName(), this.permission, this.newValue);
		else
			return Component.translatable("log.settings.permission.ally", this.player.lastKnownName(), this.permission, this.oldValue, this.newValue);
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
		if(other instanceof ChangeAllyPermissionNotification)
		{
			ChangeAllyPermissionNotification n = (ChangeAllyPermissionNotification)other;
			return n.player.is(this.player) && n.permission.equals(this.permission) && n.newValue == this.newValue && n.oldValue == this.oldValue;
		}
		return false;
	}
	
	
}
