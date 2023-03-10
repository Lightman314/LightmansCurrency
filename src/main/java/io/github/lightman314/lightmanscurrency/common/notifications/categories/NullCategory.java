package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

import javax.annotation.Nonnull;

public class NullCategory extends NotificationCategory {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "null");
	
	public static final NullCategory INSTANCE = new NullCategory();
	
	private NullCategory() {}
	
	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(Items.BARRIER); }

	@Override
	public IFormattableTextComponent getName() { return EasyText.literal("NULL"); }

	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public boolean matches(NotificationCategory other) { return other instanceof NullCategory; }

	@Override
	protected void saveAdditional(CompoundNBT compound) { }
	
}