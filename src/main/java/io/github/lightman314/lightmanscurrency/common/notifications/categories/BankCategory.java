package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class BankCategory extends NotificationCategory {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank");
	
	private final MutableComponent name;
	
	public BankCategory(MutableComponent name) { this.name = name; }
	
	public BankCategory(CompoundTag compound) {
		this.name = Component.Serializer.fromJson(compound.getString("Name"));
	}

	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.MACHINE_ATM); }

	@Override
	public MutableComponent getName() { return this.name; }

	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public boolean matches(NotificationCategory other) {
		if(other instanceof BankCategory)
		{
			BankCategory bc = (BankCategory)other;
			return bc.name.getString().equals(this.name.getString());
		}
		return false;
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.putString("Name", Component.Serializer.toJson(this.name));
	}
	
}