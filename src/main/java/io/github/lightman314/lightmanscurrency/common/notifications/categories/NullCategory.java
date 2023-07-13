package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class NullCategory extends NotificationCategory {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "null");
	
	public static final NullCategory INSTANCE = new NullCategory();
	
	private NullCategory() {}
	
	@Override
	public @NotNull IconData getIcon() { return IconData.of(Items.BARRIER); }

	@Override
	public MutableComponent getName() { return EasyText.literal("NULL"); }

	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public boolean matches(NotificationCategory other) { return other instanceof NullCategory; }

	@Override
	protected void saveAdditional(CompoundTag compound) { }
	
}
