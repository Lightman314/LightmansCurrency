package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class BankCategory extends NotificationCategory {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "bank");
	
	private final IFormattableTextComponent name;
	
	public BankCategory(IFormattableTextComponent name) { this.name = name; }
	
	public BankCategory(CompoundNBT compound) {
		this.name = ITextComponent.Serializer.fromJson(compound.getString("Name"));
	}

	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(ModBlocks.MACHINE_ATM); }

	@Override
	public IFormattableTextComponent getName() { return this.name; }

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
	protected void saveAdditional(CompoundNBT compound) {
		compound.putString("Name", ITextComponent.Serializer.toJson(this.name));
	}
	
}