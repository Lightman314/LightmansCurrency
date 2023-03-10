package io.github.lightman314.lightmanscurrency.common.notifications.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

public class TextNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "text");
	
	private IFormattableTextComponent text = EasyText.empty();
	private NotificationCategory category = NullCategory.INSTANCE;
	
	public TextNotification(IFormattableTextComponent text){ this(text, NullCategory.INSTANCE); }
	public TextNotification(IFormattableTextComponent text, NotificationCategory category) { this.text = text; this.category = category; }
	
	public TextNotification(CompoundNBT compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.category; }

	@Override
	public IFormattableTextComponent getMessage() { return text; }

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		compound.putString("Text", ITextComponent.Serializer.toJson(this.text));
		compound.put("Category", this.category.save());
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		if(compound.contains("Text", Constants.NBT.TAG_STRING))
			this.text = ITextComponent.Serializer.fromJson(compound.getString("Text"));
		if(compound.contains("Category", Constants.NBT.TAG_COMPOUND))
			this.category = NotificationCategory.deserialize(compound.getCompound("Category"));
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof TextNotification)
		{
			TextNotification otherText = (TextNotification)other;
			return otherText.text.getString().equals(this.text.getString());
		}
		return false;
	}
	
}