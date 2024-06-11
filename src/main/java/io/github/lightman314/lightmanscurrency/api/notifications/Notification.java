package io.github.lightman314.lightmanscurrency.api.notifications;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public abstract class Notification implements IClientTracker {

	private boolean isClient = false;
	@Override
	public final boolean isClient() { return this.isClient; }
	@Override
	public final boolean isServer() { return IClientTracker.super.isServer(); }

	private long timeStamp;
	public long getTimeStamp() { return this.timeStamp; }
	public final boolean hasTimeStamp() { return this.getTimeStamp() > 0; }

	private boolean seen = false;
	public boolean wasSeen() { return this.seen; }
	public void setSeen() { this.seen = true; }
	
	private int count = 1;
	public int getCount() { return this.count; }

	protected Notification() { this.timeStamp = TimeUtil.getCurrentTime(); }

	@Nonnull
	protected abstract NotificationType<?> getType();

	@Nonnull
	public abstract NotificationCategory getCategory();

	@Nonnull
	public abstract MutableComponent getMessage();

	@Nonnull
	public MutableComponent getGeneralMessage() { return LCText.NOTIFICATION_FORMAT_GENERAL.get(this.getCategory().getName(), this.getMessage()); }

	@Nonnull
	public MutableComponent getChatMessage() {
		return LCText.NOTIFICATION_FORMAT_CHAT.get(
				LCText.NOTIFICATION_FORMAT_CHAT_TITLE.get(this.getCategory().getName()).withStyle(ChatFormatting.GOLD),
				this.getMessage());
	}

	@Nonnull
	public Component getTimeStampMessage() { return LCText.NOTIFICATION_TIMESTAMP.get(TimeUtil.formatTime(this.timeStamp)); }

	@Nonnull
	public final CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		if(this.seen)
			compound.putBoolean("Seen", true);
		compound.putInt("Count", this.count);
		compound.putString("Type", this.getType().type.toString());
		if(this.timeStamp > 0)
			compound.putLong("TimeStamp", this.timeStamp);
		try {
			this.saveAdditional(compound);
		} catch (Throwable t) { LightmansCurrency.LogError("Error saving Notification of type '" + this.getType().type.toString() + "'",t); }
		return compound;
	}
	
	protected abstract void saveAdditional(@Nonnull CompoundTag compound);
	
	public final void load(@Nonnull CompoundTag compound) {
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
	
	protected abstract void loadAdditional(@Nonnull CompoundTag compound);
	
	/**
	 * Determines whether the new notification should stack or not.
	 * @param other The other notification. Use this to determine if the other notification is a duplicate or not.
	 * @return True if the notification was stacked.
	 */
	public boolean onNewNotification(@Nonnull Notification other) {
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
	protected abstract boolean canMerge(@Nonnull Notification other);

	public void flagAsClient() { this.isClient = true; }

}
