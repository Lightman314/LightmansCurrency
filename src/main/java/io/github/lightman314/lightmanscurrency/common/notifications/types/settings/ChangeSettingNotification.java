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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ChangeSettingNotification extends SingleLineNotification {

	public static final NotificationType<Advanced> ADVANCED_TYPE = new NotificationType<>(VersionUtil.lcResource("change_settings_advanced"),ChangeSettingNotification::createAdvanced);
	public static final NotificationType<Simple> SIMPLE_TYPE = new NotificationType<>(VersionUtil.lcResource("change_settings_simple"),ChangeSettingNotification::createSimple);
	public static final NotificationType<Dumb> DUMB_TYPE = new NotificationType<>(VersionUtil.lcResource("change_settings_dumb"),ChangeSettingNotification::createDumb);

	protected PlayerReference player;
	protected Component setting;
	
	protected ChangeSettingNotification(PlayerReference player, Component setting) { this.player = player; this.setting = setting; }
	protected ChangeSettingNotification() {}

	@Nullable
	public static ChangeSettingNotification dumb(@Nullable PlayerReference player, Component setting) { return player == null ? null : new Dumb(player,setting); }
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
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		compound.put("Player", this.player.save());
		compound.putString("Setting", Component.Serializer.toJson(this.setting,lookup));
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.setting = EasyText.loadComponentOrString(compound.getString("Setting"),lookup);
	}

	private static Advanced createAdvanced() { return new Advanced(); }
	private static Simple createSimple() { return new Simple(); }
	private static Dumb createDumb() { return new Dumb(); }

	public static class Advanced extends ChangeSettingNotification
	{

		Component newValue;
		Component oldValue;

		private Advanced() { }
		private Advanced(PlayerReference player, Component setting, Component newValue, Component oldValue) { super(player, setting); this.newValue = newValue; this.oldValue = oldValue; }
		
        @Override
		protected NotificationType<Advanced> getType() { return ADVANCED_TYPE; }

		
		@Override
		public Component getMessage() { return LCText.NOTIFICATION_SETTINGS_CHANGE_ADVANCED.get(this.player.getName(this.isClient()), this.setting, this.oldValue, this.newValue); }

		@Override
		protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
			super.saveAdditional(compound,lookup);
			compound.putString("NewValue", Component.Serializer.toJson(this.newValue,lookup));
			compound.putString("OldValue", Component.Serializer.toJson(this.oldValue,lookup));
		}
		
		@Override
		protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
			super.loadAdditional(compound,lookup);
			this.newValue = EasyText.loadComponentOrString(compound.getString("NewValue"),lookup);
			this.oldValue = EasyText.loadComponentOrString(compound.getString("OldValue"),lookup);
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
		public Component getMessage() {
			return LCText.NOTIFICATION_SETTINGS_CHANGE_SIMPLE.get(this.player.getName(this.isClient()), this.setting, this.newValue);
		}
		
		@Override
		protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
			super.saveAdditional(compound,lookup);
			compound.putString("NewValue",Component.Serializer.toJson(this.newValue,lookup));
		}
		
		@Override
		protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
			super.loadAdditional(compound,lookup);
			this.newValue = EasyText.loadComponentOrString(compound.getString("NewValue"),lookup);
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


	public static class Dumb extends ChangeSettingNotification
	{

		private Dumb() {}
		private Dumb(PlayerReference player, Component setting) { super(player,setting); }

		@Override
		protected Component getMessage() { return LCText.NOTIFICATION_SETTINGS_CHANGE_DUMB.get(this.player.getName(this.isClient()),this.setting); }

		@Override
		protected NotificationType<?> getType() { return DUMB_TYPE; }

		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Dumb d)
				return d.player.equals(this.player) && d.setting.equals(this.setting);
			return false;
		}
	}
	
}
