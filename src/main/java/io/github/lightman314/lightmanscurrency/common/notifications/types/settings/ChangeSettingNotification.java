package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public abstract class ChangeSettingNotification extends Notification {

	public static final ResourceLocation ADVANCED_TYPE = new ResourceLocation(LightmansCurrency.MODID, "change_settings_advanced");
	public static final ResourceLocation SIMPLE_TYPE = new ResourceLocation(LightmansCurrency.MODID, "change_settings_simple");
	
	protected PlayerReference player;
	protected String setting;
	
	protected ChangeSettingNotification(PlayerReference player, String setting) { this.player = player; this.setting = setting; }
	protected ChangeSettingNotification() {}

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		compound.put("Player", this.player.save());
		compound.putString("Setting", this.setting);
	}
	
	@Override
	protected void loadAdditional(CompoundNBT compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.setting = compound.getString("Setting");
	}
	
	public static class Advanced extends ChangeSettingNotification
	{
		
		String newValue;
		String oldValue;
		
		public Advanced(PlayerReference player, String setting, String newValue, String oldValue) { super(player, setting); this.newValue = newValue; this.oldValue = oldValue; }
		public Advanced(CompoundNBT compound) { this.load(compound); }
		
		@Override
		protected ResourceLocation getType() { return ADVANCED_TYPE; }

		@Override
		public IFormattableTextComponent getMessage() { return EasyText.translatable("log.settings.change", this.player.getName(true), this.setting, this.oldValue, this.newValue); }

		@Override
		protected void saveAdditional(CompoundNBT compound) {
			super.saveAdditional(compound);
			compound.putString("NewValue", this.newValue);
			compound.putString("OldValue", this.oldValue);
		}
		
		@Override
		protected void loadAdditional(CompoundNBT compound) {
			super.loadAdditional(compound);
			this.newValue = compound.getString("NewValue");
			this.oldValue = compound.getString("OldValue");
		}
		
		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Advanced)
			{
				Advanced n = (Advanced)other;
				return n.player.is(this.player) && n.setting.equals(this.setting) && n.newValue.equals(this.newValue) && n.oldValue.equals(this.oldValue);
			}
			return false;
		}
		
	}
	
	public static class Simple extends ChangeSettingNotification
	{

		String newValue;
		
		public Simple(PlayerReference player, String setting, String newValue) { super(player, setting); this.newValue = newValue; }
		public Simple(CompoundNBT compound) { this.load(compound); }
		
		@Override
		protected ResourceLocation getType() { return SIMPLE_TYPE; }

		@Override
		public IFormattableTextComponent getMessage() {
			return EasyText.translatable("log.settings.change.simple", this.player.getName(true), this.setting, this.newValue);
		}
		
		@Override
		protected void saveAdditional(CompoundNBT compound) {
			super.saveAdditional(compound);
			compound.putString("NewValue", this.newValue);
		}
		
		@Override
		protected void loadAdditional(CompoundNBT compound) {
			super.loadAdditional(compound);
			this.newValue = compound.getString("NewValue");
		}

		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Simple)
			{
				Simple n = (Simple)other;
				return n.player.is(this.player) && n.setting.equals(this.setting) && n.newValue.equals(this.newValue);
			}
			return false;
		}
		
	}
	
}