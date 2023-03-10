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

public class AddRemoveTradeNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "add_remove_trade");
	
	PlayerReference player;
	boolean isAdd;
	int newCount;
	
	public AddRemoveTradeNotification(PlayerReference player, boolean isAdd, int newCount) { this.player = player; this.isAdd = isAdd; this.newCount = newCount; }
	public AddRemoveTradeNotification(CompoundNBT compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public IFormattableTextComponent getMessage() {
		return EasyText.translatable("log.settings.addremovetrade", this.player.getName(true), EasyText.translatable(this.isAdd ? "log.settings.add" : "log.settings.remove"), newCount);
	}

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		compound.put("Player", this.player.save());
		compound.putBoolean("Add", this.isAdd);
		compound.putInt("NewCount", this.newCount);
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.isAdd = compound.getBoolean("Add");
		this.newCount = compound.getInt("NewCount");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof AddRemoveTradeNotification)
		{
			AddRemoveTradeNotification n = (AddRemoveTradeNotification)other;
			return n.player.is(this.player) && this.isAdd == n.isAdd && this.newCount == n.newCount;
		}
		return false;
	}

}