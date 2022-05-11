package io.github.lightman314.lightmanscurrency.common.notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton.ITab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Notification {

	private static final Map<String,Function<CompoundTag,Notification>> DESERIALIZERS = new HashMap<>();
	
	public static final void register(ResourceLocation type, Function<CompoundTag,Notification> deserializer) {
		String t = type.toString();
		if(DESERIALIZERS.containsKey(t))
		{
			LightmansCurrency.LogError("Notification of type " + t + " is already registered.");
			return;
		}
		if(deserializer == null)
		{
			LightmansCurrency.LogError("Deserializer of notification type " + t + " is null. Unable to register.");
			return;
		}
		DESERIALIZERS.put(t, deserializer);
	}
	
	public static final Notification deserialize(CompoundTag compound) {
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
	
	private boolean seen = false;
	public boolean wasSeen() { return this.seen; }
	public void setSeen() { this.seen = true; }
	
	private int count = 1;
	public int getCount() { return this.count; }
	
	protected abstract ResourceLocation getType();
	
	public abstract Category getCategory();
	
	public abstract Component getMessage();
	
	public final CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		if(this.seen)
			compound.putBoolean("Seen", true);
		compound.putInt("Count", this.count);
		compound.putString("type", this.getType().toString());
		this.saveAdditional(compound);
		return compound;
	}
	
	protected abstract void saveAdditional(CompoundTag compound);
	
	public final void load(CompoundTag compound) {
		if(compound.contains("Seen"))
			this.seen = true;
		if(compound.contains("Count", Tag.TAG_INT))
			this.count = compound.getInt("Count");
		this.loadAdditional(compound);
	}
	
	protected abstract void loadAdditional(CompoundTag compound);
	
	/**
	 * Determines whether the new notification should stack or not.
	 * @param other The other notification. Use this to determine if the other notification is a duplicate or not.
	 * @return True if the notification was stacked.
	 */
	public boolean onNewNotification(Notification other) {
		if(canMerge(other))
		{
			this.count++;
			this.seen = false;
			return true;
		}
		return false;
	}
	
	/**
	 * Whether the other notification should be merged with this one.
	 */
	protected abstract boolean canMerge(Notification other);
	
	
	public static abstract class Category implements ITab
	{
		
		public static final ResourceLocation GENERAL_TYPE = new ResourceLocation(LightmansCurrency.MODID, "general");
		
		private static final Map<String,Function<CompoundTag,Category>> DESERIALIZERS = new HashMap<>();
		
		public static final void register(ResourceLocation type, Function<CompoundTag,Category> deserializer) {
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
		
		public static final Category deserialize(CompoundTag compound) {
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
		
		@OnlyIn(Dist.CLIENT)
		public abstract IconData getIcon();
		public abstract Component getTooltip();
		public final int getColor() { return 0xFFFFFF; }
		protected abstract ResourceLocation getType();
		
		public abstract boolean matches(Category other);
		
		public static final Category GENERAL = new Category() {
			@Override
			public IconData getIcon() { return IconData.of(Items.CHEST); }
			@Override
			public Component getTooltip() { return new TranslatableComponent("notifications.source.general"); }
			@Override
			public boolean matches(Category other) { return other == GENERAL; }
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
		
	}
	
}
