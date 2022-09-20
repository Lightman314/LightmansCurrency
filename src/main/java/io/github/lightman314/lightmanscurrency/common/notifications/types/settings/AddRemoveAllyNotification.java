package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class AddRemoveAllyNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "add_remove_ally");
	
	PlayerReference player;
	boolean isAdd;
	PlayerReference ally;
	
	public AddRemoveAllyNotification(PlayerReference player, boolean isAdd, PlayerReference ally) {
		this.player = player;
		this.isAdd = isAdd;
		this.ally = ally;
	}
	public AddRemoveAllyNotification(CompoundTag compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public MutableComponent getMessage() {
		return new TranslatableComponent("log.settings.addremoveally", this.player.lastKnownName(), new TranslatableComponent(this.isAdd ? "log.settings.add" : "log.settings.remove"), this.ally.lastKnownName(), new TranslatableComponent(this.isAdd ? "log.settings.to" : "log.settings.from"));
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.putBoolean("Add", this.isAdd);
		compound.put("Ally", this.ally.save());
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.isAdd = compound.getBoolean("Add");
		this.ally = PlayerReference.load(compound.getCompound("Ally"));
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof AddRemoveAllyNotification)
		{
			AddRemoveAllyNotification n = (AddRemoveAllyNotification)other;
			return n.player.is(this.player) && n.isAdd == this.isAdd && n.ally.is(this.ally);
		}
		return false;
	}

}