package io.github.lightman314.lightmanscurrency.common.notifications.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class TextNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "text");
	
	private MutableComponent text = EasyText.literal("");
	private NotificationCategory category = NullCategory.INSTANCE;
	
	public TextNotification(MutableComponent text){ this(text, NullCategory.INSTANCE); }
	public TextNotification(MutableComponent text, NotificationCategory category) { this.text = text; this.category = category; }
	
	public TextNotification(CompoundTag compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.category; }

	@Override
	public MutableComponent getMessage() { return text; }

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.putString("Text", EasyText.Serializer.toJson(this.text));
		compound.put("Category", this.category.save());
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		if(compound.contains("Text", Tag.TAG_STRING))
			this.text = EasyText.Serializer.fromJson(compound.getString("Text"));
		if(compound.contains("Category", Tag.TAG_COMPOUND))
			this.category = NotificationCategory.deserialize(compound.getCompound("Category"));
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof TextNotification otherText)
			return otherText.text.getString().equals(this.text.getString());
		return false;
	}
	
}
