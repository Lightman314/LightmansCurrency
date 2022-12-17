package io.github.lightman314.lightmanscurrency.common.notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public abstract class Notification {

	private static final Map<String,Function<CompoundTag,Notification>> DESERIALIZERS = new HashMap<>();
	
	public static void register(ResourceLocation type, Supplier<Notification> deserializer) {
		register(type, c -> {
			Notification n = deserializer.get();
			n.load(c);
			return n;
		});
	}
	
	public static void register(ResourceLocation type, Function<CompoundTag,Notification> deserializer) {
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
	
	public static Notification deserialize(CompoundTag compound) {
		if(compound.contains("Type") || compound.contains("type"))
		{
			String type = compound.contains("Type") ? compound.getString("Type") : compound.getString("type");
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

	private long timeStamp;
	public long getTimeStamp() { return this.timeStamp; }
	public final boolean hasTimeStamp() { return this.getTimeStamp() > 0; }

	private boolean seen = false;
	public boolean wasSeen() { return this.seen; }
	public void setSeen() { this.seen = true; }
	
	private int count = 1;
	public int getCount() { return this.count; }

	protected Notification() { this.timeStamp = TimeUtil.getCurrentTime(); }
	
	protected abstract ResourceLocation getType();
	
	public abstract NotificationCategory getCategory();
	
	public abstract MutableComponent getMessage();
	
	public MutableComponent getGeneralMessage() {
		return new TranslatableComponent("notifications.source.general.format", this.getCategory().getName(), this.getMessage());
	}
	
	public MutableComponent getChatMessage() {
		return new TranslatableComponent("notifications.chat.format",
				new TranslatableComponent("notifications.chat.format.title", this.getCategory().getName()).withStyle(ChatFormatting.GOLD),
				this.getMessage());
	}

	public MutableComponent getTimeStampMessage() { return new TranslatableComponent("notifications.timestamp",TimeUtil.formatTime(this.timeStamp)); }
	
	public final CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		if(this.seen)
			compound.putBoolean("Seen", true);
		compound.putInt("Count", this.count);
		compound.putString("Type", this.getType().toString());
		if(this.timeStamp > 0)
			compound.putLong("TimeStamp", this.timeStamp);
		this.saveAdditional(compound);
		return compound;
	}
	
	protected abstract void saveAdditional(CompoundTag compound);
	
	public final void load(CompoundTag compound) {
		if(compound.contains("Seen"))
			this.seen = true;
		if(compound.contains("Count", Tag.TAG_INT))
			this.count = compound.getInt("Count");
		if(compound.contains("TimeStamp", Tag.TAG_LONG))
			this.timeStamp = compound.getLong("TimeStamp");
		else
			this.timeStamp = 0;
		this.loadAdditional(compound);
	}
	
	protected abstract void loadAdditional(CompoundTag compound);
	
	/**
	 * Determines whether the new notification should stack or not.
	 * @param other The other notification. Use this to determine if the other notification is a duplicate or not.
	 * @return True if the notification was stacked.
	 */
	public boolean onNewNotification(Notification other) {
		if(this.canMerge(other))
		{
			this.count++;
			this.seen = false;
			this.timeStamp = TimeUtil.getCurrentTime();
			return true;
		}
		return false;
	}
	
	/**
	 * Whether the other notification should be merged with this one.
	 */
	protected abstract boolean canMerge(Notification other);
	
}