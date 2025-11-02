package io.github.lightman314.lightmanscurrency.common.notifications.types;

import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TextNotification extends SingleLineNotification {

	public static final NotificationType<TextNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("text"),TextNotification::new);
	
	private Component text = EasyText.literal("");
	private NotificationCategory category = NullCategory.INSTANCE;

	private TextNotification() {}

	public TextNotification(Component text){ this(text, NullCategory.INSTANCE); }
	public TextNotification(Component text, NotificationCategory category) { this.text = text; this.category = category; }

	public static NonNullSupplier<Notification> create(Component text) { return () -> new TextNotification(text); }
	public static NonNullSupplier<Notification> create(Component text, NotificationCategory category) { return () -> new TextNotification(text, category); }

    @Override
	protected NotificationType<TextNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.category; }

	@Override
	public Component getMessage() { return this.text; }

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.putString("Text", Component.Serializer.toJson(this.text));
		compound.put("Category", this.category.save());
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		if(compound.contains("Text", Tag.TAG_STRING))
			this.text = Component.Serializer.fromJson(compound.getString("Text"));
		if(compound.contains("Category", Tag.TAG_COMPOUND))
			this.category = NotificationAPI.getApi().LoadCategory(compound.getCompound("Category"));
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof TextNotification otherText)
			return otherText.text.equals(this.text) && otherText.category.matches(this.category);
		return false;
	}
	
}
