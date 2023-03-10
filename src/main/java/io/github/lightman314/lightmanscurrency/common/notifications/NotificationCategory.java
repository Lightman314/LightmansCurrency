package io.github.lightman314.lightmanscurrency.common.notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton.ITab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

import javax.annotation.Nonnull;

public abstract class NotificationCategory implements ITab
{
	
	public static final ResourceLocation GENERAL_TYPE = new ResourceLocation(LightmansCurrency.MODID, "general");
	
	private static final Map<String,Function<CompoundNBT,NotificationCategory>> DESERIALIZERS = new HashMap<>();
	
	public static void register(ResourceLocation type, Function<CompoundNBT,NotificationCategory> deserializer) {
		String t = type.toString();
		if(DESERIALIZERS.containsKey(t))
		{
			LightmansCurrency.LogError("Category of type " + t + " is already registered.");
			return;
		}
		if(deserializer == null)
		{
			LightmansCurrency.LogError("Deserializer of category type " + t + " is null. Unable to register.");
			return;
		}
		DESERIALIZERS.put(t, deserializer);
	}
	
	public static NotificationCategory deserialize(CompoundNBT compound) {
		if(compound.contains("type"))
		{
			String type = compound.getString("type");
			if(DESERIALIZERS.containsKey(type))
			{
				return DESERIALIZERS.get(type).apply(compound);
			}
			else
			{
				LightmansCurrency.LogError("Cannot deserialize notification type " + type + " as no deserializer has been registered.");
				return null;
			}
		}
		else
		{
			LightmansCurrency.LogError("Cannot deserialize notification as tag is missing the 'type' tag.");
			return null;
		}
	}
	
	/* Obsolete as this is covered by ITab
	public abstract IconData getIcon();
	*/
	public final IFormattableTextComponent getTooltip() { return this.getName(); }
	public abstract IFormattableTextComponent getName();
	public final int getColor() { return 0xFFFFFF; }
	protected abstract ResourceLocation getType();
	
	public abstract boolean matches(NotificationCategory other);
	
	public static final NotificationCategory GENERAL = new NotificationCategory() {
		@Override
		public @Nonnull IconData getIcon() { return IconData.of(Items.CHEST); }
		@Override
		public IFormattableTextComponent getName() { return EasyText.translatable("notifications.source.general"); }
		@Override
		public boolean matches(NotificationCategory other) { return other == GENERAL; }
		@Override
		protected ResourceLocation getType() { return GENERAL_TYPE; }
		@Override
		protected void saveAdditional(CompoundNBT compound) {}
	};
	
	public final CompoundNBT save() {
		CompoundNBT compound = new CompoundNBT();
		compound.putString("type", this.getType().toString());
		this.saveAdditional(compound);
		return compound;
	}
	
	protected abstract void saveAdditional(CompoundNBT compound);

	public final boolean notGeneral() { return this != GENERAL; }
	
}