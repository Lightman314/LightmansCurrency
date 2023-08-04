package io.github.lightman314.lightmanscurrency.common.notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public abstract class NotificationCategory implements ITab
{
	
	public static final ResourceLocation GENERAL_TYPE = new ResourceLocation(LightmansCurrency.MODID, "general");
	
	private static final Map<String,Function<CompoundTag,NotificationCategory>> DESERIALIZERS = new HashMap<>();

	public static void registerInstance(ResourceLocation type, NotificationCategory instance) { register(type, c -> instance); }
	public static void register(ResourceLocation type, Function<CompoundTag,NotificationCategory> deserializer) {
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
	
	public static NotificationCategory deserialize(CompoundTag compound) {
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
				return NotificationCategory.GENERAL;
			}
		}
		else
		{
			LightmansCurrency.LogError("Cannot deserialize notification as tag is missing the 'type' tag.");
			return NotificationCategory.GENERAL;
		}
	}

	public final MutableComponent getTooltip() { return this.getName(); }
	public abstract MutableComponent getName();
	public final int getColor() { return 0xFFFFFF; }
	protected abstract ResourceLocation getType();
	
	public abstract boolean matches(NotificationCategory other);
	
	public static final NotificationCategory GENERAL = new NotificationCategory() {
		@Override
		public @NotNull IconData getIcon() { return IconData.of(Items.CHEST); }
		@Override
		public MutableComponent getName() { return EasyText.translatable("notifications.source.general"); }
		@Override
		public boolean matches(NotificationCategory other) { return other == GENERAL; }
		@Override
		protected ResourceLocation getType() { return GENERAL_TYPE; }
		@Override
		protected void saveAdditional(CompoundTag compound) {}
	};
	
	public final CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		compound.putString("type", this.getType().toString());
		this.saveAdditional(compound);
		return compound;
	}
	
	protected abstract void saveAdditional(CompoundTag compound);

	public final boolean notGeneral() { return this != GENERAL; }
	
}
