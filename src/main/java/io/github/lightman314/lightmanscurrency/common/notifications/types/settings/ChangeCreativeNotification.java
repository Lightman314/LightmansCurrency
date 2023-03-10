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

public class ChangeCreativeNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "change_creative");
	
	PlayerReference player;
	boolean creative;
	
	public ChangeCreativeNotification(PlayerReference player, boolean creative) { this.player = player; this.creative = creative; }
	public ChangeCreativeNotification(CompoundNBT compound) { this.load(compound); }
	@Override
	protected ResourceLocation getType() { return TYPE; }
	
	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }
	
	@Override
	public IFormattableTextComponent getMessage() {
		return EasyText.translatable("log.settings.creativemode", this.player.getName(true), EasyText.translatable(this.creative ? "log.settings.enabled" : "log.settings.disabled"));
	}
	
	@Override
	protected void saveAdditional(CompoundNBT compound) {
		compound.put("Player", this.player.save());
		compound.putBoolean("Creative", this.creative);
	}
	
	@Override
	protected void loadAdditional(CompoundNBT compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.creative = compound.getBoolean("Creative");
	}
	
	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ChangeCreativeNotification)
		{
			ChangeCreativeNotification n = (ChangeCreativeNotification)other;
			return n.player.is(this.player) && n.creative == this.creative;
		}
		return false;
	}
	
}