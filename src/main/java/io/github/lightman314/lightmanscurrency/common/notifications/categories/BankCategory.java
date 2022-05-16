package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification.Category;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BankCategory extends Category {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank");
	
	private final Component name;
	
	public BankCategory(Component name) { this.name = name; }
	
	public BankCategory(CompoundTag compound) {
		this.name = Component.Serializer.fromJson(compound.getString("Name"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IconData getIcon() { return IconData.of(ModBlocks.MACHINE_ATM); }

	@Override
	public Component getTooltip() { return this.name; }

	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public boolean matches(Category other) {
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
