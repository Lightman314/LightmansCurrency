package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
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
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ChangeSettingNotification extends SingleLineNotification {

	public static final NotificationType<Advanced> ADVANCED_TYPE = new NotificationType<>(VersionUtil.lcResource("change_settings_advanced"),ChangeSettingNotification::createAdvanced);
	public static final NotificationType<Simple> SIMPLE_TYPE = new NotificationType<>(VersionUtil.lcResource("change_settings_simple"),ChangeSettingNotification::createSimple);
	
	protected PlayerReference player;
	protected Component setting;
	
	protected ChangeSettingNotification(PlayerReference player, Component setting) { this.player = player; this.setting = setting; }
	protected ChangeSettingNotification() {}

	@Nullable
	public static ChangeSettingNotification simple(@Nullable PlayerReference player, Component setting, int newValue) { return simple(player,setting,String.valueOf(newValue)); }
	@Nullable
	public static ChangeSettingNotification simple(@Nullable PlayerReference player, Component setting, boolean newValue) { return simple(player,setting,LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.get(newValue).get()); }
	@Nullable
	public static ChangeSettingNotification simple(@Nullable PlayerReference player, Component setting, String newValue) { return simple(player,setting,EasyText.literal(newValue)); }
	@Nullable
	public static ChangeSettingNotification simple(@Nullable PlayerReference player, Component setting, Component newValue) { return player == null ? null : new Simple(player,setting,newValue); }

	@Nullable
	public static ChangeSettingNotification advanced(@Nullable PlayerReference player, Component setting, int newValue, int oldValue) { return advanced(player,setting,String.valueOf(newValue),String.valueOf(oldValue)); }
	public static ChangeSettingNotification advanced(@Nullable PlayerReference player, Component setting, String newValue, String oldValue) { return advanced(player,setting,EasyText.literal(newValue),EasyText.literal(oldValue)); }
	public static ChangeSettingNotification advanced(@Nullable PlayerReference player, Component setting, Component newValue, Component oldValue) { return player == null ? null : new Advanced(player,setting,newValue,oldValue); }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.putString("Settings", Component.Serializer.toJson(this.setting));
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.setting = EasyText.loadComponentOrString(compound.getString("Setting"));
	}

	private static Advanced createAdvanced() { return new Advanced(); }
	private static Simple createSimple() { return new Simple(); }

	public static class Advanced extends ChangeSettingNotification
	{
		
		Component newValue;
		Component oldValue;

		private Advanced() { }
		private Advanced(PlayerReference player, Component setting, Component newValue, Component oldValue) { super(player,setting); this.newValue = newValue; this.oldValue = oldValue; }

		
        @Override
		protected NotificationType<Advanced> getType() { return ADVANCED_TYPE; }

		
		@Override
		public MutableComponent getMessage() { return LCText.NOTIFICATION_SETTINGS_CHANGE_ADVANCED.get(this.player.getName(true), this.setting, this.oldValue, this.newValue); }

		@Override
		protected void saveAdditional(CompoundTag compound) {
			super.saveAdditional(compound);
			compound.putString("NewValue", Component.Serializer.toJson(this.newValue));
			compound.putString("OldValue", Component.Serializer.toJson(this.oldValue));
		}
		
		@Override
		protected void loadAdditional(CompoundTag compound) {
			super.loadAdditional(compound);
			this.newValue = EasyText.loadComponentOrString(compound.getString("NewValue"));
			this.oldValue = EasyText.loadComponentOrString(compound.getString("OldValue"));
		}
		
		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Advanced n)
			{
				return n.player.is(this.player) && n.setting.equals(this.setting) && n.newValue.equals(this.newValue) && n.oldValue.equals(this.oldValue);
			}
			return false;
		}
		
	}
	
	public static class Simple extends ChangeSettingNotification
	{

		Component newValue;

		private Simple() {}
		private Simple(PlayerReference player, Component setting, Component newValue) { super(player, setting); this.newValue = newValue; }
		
        @Override
		protected NotificationType<Simple> getType() { return SIMPLE_TYPE; }

		
		@Override
		public MutableComponent getMessage() {
			return LCText.NOTIFICATION_SETTINGS_CHANGE_SIMPLE.get(this.player.getName(true), this.setting, this.newValue);
		}
		
		@Override
		protected void saveAdditional(CompoundTag compound) {
			super.saveAdditional(compound);
			compound.putString("NewValue", Component.Serializer.toJson(this.newValue));
		}
		
		@Override
		protected void loadAdditional(CompoundTag compound) {
			super.loadAdditional(compound);
			this.newValue = EasyText.loadComponentOrString(compound.getString("NewValue"));
		}

		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Simple n)
			{
				return n.player.is(this.player) && n.setting.equals(this.setting) && n.newValue.equals(this.newValue);
			}
			return false;
		}
		
	}
	
	
}
