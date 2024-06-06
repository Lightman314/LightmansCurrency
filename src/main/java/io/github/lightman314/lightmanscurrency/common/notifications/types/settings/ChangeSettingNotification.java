package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LCText;
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

public abstract class ChangeSettingNotification extends Notification {

	public static final NotificationType<Advanced> ADVANCED_TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "change_settings_advanced"),ChangeSettingNotification::createAdvanced);
	public static final NotificationType<Simple> SIMPLE_TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "change_settings_simple"),ChangeSettingNotification::createSimple);
	
	protected PlayerReference player;
	protected String setting;
	
	protected ChangeSettingNotification(PlayerReference player, String setting) { this.player = player; this.setting = setting; }
	protected ChangeSettingNotification() {}

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.putString("Setting", this.setting);
	}
	
	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.setting = compound.getString("Setting");
	}

	private static Advanced createAdvanced() { return new Advanced(); }
	private static Simple createSimple() { return new Simple(); }

	public static class Advanced extends ChangeSettingNotification
	{
		
		String newValue;
		String oldValue;


		private Advanced() { }
		public Advanced(PlayerReference player, String setting, String newValue, String oldValue) { super(player, setting); this.newValue = newValue; this.oldValue = oldValue; }
		
		@Nonnull
        @Override
		protected NotificationType<Advanced> getType() { return ADVANCED_TYPE; }

		@Nonnull
		@Override
		public MutableComponent getMessage() { return LCText.NOTIFICATION_SETTINGS_CHANGE_ADVANCED.get(this.player.getName(true), this.setting, this.oldValue, this.newValue); }

		@Override
		protected void saveAdditional(@Nonnull CompoundTag compound) {
			super.saveAdditional(compound);
			compound.putString("NewValue", this.newValue);
			compound.putString("OldValue", this.oldValue);
		}
		
		@Override
		protected void loadAdditional(@Nonnull CompoundTag compound) {
			super.loadAdditional(compound);
			this.newValue = compound.getString("NewValue");
			this.oldValue = compound.getString("OldValue");
		}
		
		@Override
		protected boolean canMerge(@Nonnull Notification other) {
			if(other instanceof Advanced n)
			{
				return n.player.is(this.player) && n.setting.equals(this.setting) && n.newValue.equals(this.newValue) && n.oldValue.equals(this.oldValue);
			}
			return false;
		}
		
	}
	
	public static class Simple extends ChangeSettingNotification
	{

		String newValue;

		private Simple() {}
		public Simple(PlayerReference player, String setting, String newValue) { super(player, setting); this.newValue = newValue; }
		
		@Nonnull
        @Override
		protected NotificationType<Simple> getType() { return SIMPLE_TYPE; }

		@Nonnull
		@Override
		public MutableComponent getMessage() {
			return LCText.NOTIFICATION_SETTINGS_CHANGE_SIMPLE.get(this.player.getName(true), this.setting, this.newValue);
		}
		
		@Override
		protected void saveAdditional(@Nonnull CompoundTag compound) {
			super.saveAdditional(compound);
			compound.putString("NewValue", this.newValue);
		}
		
		@Override
		protected void loadAdditional(@Nonnull CompoundTag compound) {
			super.loadAdditional(compound);
			this.newValue = compound.getString("NewValue");
		}

		@Override
		protected boolean canMerge(@Nonnull Notification other) {
			if(other instanceof Simple n)
			{
				return n.player.is(this.player) && n.setting.equals(this.setting) && n.newValue.equals(this.newValue);
			}
			return false;
		}
		
	}
	
	
}
