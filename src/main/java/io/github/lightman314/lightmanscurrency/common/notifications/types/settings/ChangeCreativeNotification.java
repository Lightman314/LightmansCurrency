package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ChangeCreativeNotification extends Notification {

	public static final NotificationType<ChangeCreativeNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "change_creative"),ChangeCreativeNotification::new);
	
	PlayerReference player;
	boolean creative;

	private ChangeCreativeNotification() {}
	public ChangeCreativeNotification(PlayerReference player, boolean creative) { this.player = player; this.creative = creative; }
	@Nonnull
    @Override
	protected NotificationType<ChangeCreativeNotification> getType() { return TYPE; }
	
	@Nonnull
	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }
	
	@Nonnull
	@Override
	public MutableComponent getMessage() {
		return Component.translatable("log.settings.creativemode", this.player.getName(true), Component.translatable(this.creative ? "log.settings.enabled" : "log.settings.disabled"));
	}
	
	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.putBoolean("Creative", this.creative);
	}
	
	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.creative = compound.getBoolean("Creative");
	}
	
	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof ChangeCreativeNotification n)
		{
			return n.player.is(this.player) && n.creative == this.creative;
		}
		return false;
	}
	
}
