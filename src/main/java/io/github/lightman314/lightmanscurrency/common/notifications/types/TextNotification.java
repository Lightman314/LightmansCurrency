package io.github.lightman314.lightmanscurrency.common.notifications.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class TextNotification extends Notification {

	public static final NotificationType<TextNotification> TYPE = new NotificationType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "text"),TextNotification::new);
	
	private MutableComponent text = EasyText.literal("");
	private NotificationCategory category = NullCategory.INSTANCE;

	private TextNotification() {}

	public TextNotification(MutableComponent text){ this(text, NullCategory.INSTANCE); }
	public TextNotification(MutableComponent text, NotificationCategory category) { this.text = text; this.category = category; }

	public static Supplier<Notification> create(MutableComponent text) { return () -> new TextNotification(text); }
	public static Supplier<Notification> create(MutableComponent text, NotificationCategory category) { return () -> new TextNotification(text, category); }

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
	protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		compound.putString("Text", Component.Serializer.toJson(this.text,lookup));
		compound.put("Category", this.category.save(lookup));
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		if(compound.contains("Text", Tag.TAG_STRING))
			this.text = Component.Serializer.fromJson(compound.getString("Text"),lookup);
		if(compound.contains("Category", Tag.TAG_COMPOUND))
			this.category = NotificationAPI.API.LoadCategory(compound.getCompound("Category"),lookup);
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof TextNotification otherText)
			return otherText.text.getString().equals(this.text.getString()) && otherText.category.matches(this.category);
		return false;
	}
	
}
