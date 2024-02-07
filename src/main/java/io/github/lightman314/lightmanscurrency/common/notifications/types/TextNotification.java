package io.github.lightman314.lightmanscurrency.common.notifications.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class TextNotification extends Notification {

	public static final NotificationType<TextNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "text"),TextNotification::new);
	
	private MutableComponent text = EasyText.literal("");
	private NotificationCategory category = NullCategory.INSTANCE;

	private TextNotification() {}

	public TextNotification(MutableComponent text){ this(text, NullCategory.INSTANCE); }
	public TextNotification(MutableComponent text, NotificationCategory category) { this.text = text; this.category = category; }

	public static NonNullSupplier<Notification> create(MutableComponent text) { return () -> new TextNotification(text); }
	public static NonNullSupplier<Notification> create(MutableComponent text, NotificationCategory category) { return () -> new TextNotification(text, category); }

	@Nonnull
    @Override
	protected NotificationType<TextNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return this.category; }

	@Nonnull
	@Override
	public MutableComponent getMessage() { return text; }

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		compound.putString("Text", Component.Serializer.toJson(this.text));
		compound.put("Category", this.category.save());
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		if(compound.contains("Text", Tag.TAG_STRING))
			this.text = Component.Serializer.fromJson(compound.getString("Text"));
		if(compound.contains("Category", Tag.TAG_COMPOUND))
			this.category = NotificationAPI.loadCategory(compound.getCompound("Category"));
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof TextNotification otherText)
			return otherText.text.getString().equals(this.text.getString()) && otherText.category.matches(this.category);
		return false;
	}
	
}
