package io.github.lightman314.lightmanscurrency.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class IconData {

	private static IconData NULL = null;
	@Nonnull
	public static IconData Null() {
		if(NULL == null)
			NULL = new NullIcon();
		return NULL;
	}

	private static final Map<ResourceLocation,Function<CompoundTag,IconData>> ICON_TYPES = new HashMap<>();
	public static void registerIconType(@Nonnull ResourceLocation type, @Nonnull Function<CompoundTag,IconData> deserializer)
	{
		if(ICON_TYPES.containsKey(type))
		{
			LightmansCurrency.LogDebug("Attempted to register icon of type '" + type + "' twice!");
			return;
		}
		ICON_TYPES.put(type,deserializer);
	}

	public static void registerDefaultIcons() {
		if(ICON_TYPES.containsKey(NullIcon.TYPE))
		{
			LightmansCurrency.LogWarning("Attempted to register the default icons twice!");
			return;
		}
		registerIconType(NullIcon.TYPE,(c) -> IconData.NULL);
		registerIconType(ItemIcon.TYPE,ItemIcon::loadItem);
		registerIconType(ImageIcon.TYPE,ImageIcon::loadImage);
		registerIconType(TextIcon.TYPE,TextIcon::loadText);
		registerIconType(MultiIcon.TYPE,MultiIcon::loadMulti);
	}

	@Nullable
	public static IconData load(@Nonnull CompoundTag tag)
	{
		if(tag.contains("Type"))
		{
			ResourceLocation type = new ResourceLocation(tag.getString("Type"));
			if(ICON_TYPES.containsKey(type))
				return ICON_TYPES.get(type).apply(tag);
		}
		return null;
	}

	protected final ResourceLocation type;
	public final boolean isNull() { return this instanceof NullIcon; }
	protected IconData(@Nonnull ResourceLocation type) { this.type = type; }

	@OnlyIn(Dist.CLIENT)
	public final void render(@Nonnull EasyGuiGraphics gui, @Nonnull ScreenPosition pos) { this.render(gui, pos.x, pos.y); }
	@OnlyIn(Dist.CLIENT)
	public abstract void render(@Nonnull EasyGuiGraphics gui, int x, int y);

	@Nonnull
	public CompoundTag save()
	{
		CompoundTag tag = new CompoundTag();
		this.saveAdditional(tag);
		tag.putString("Type", this.type.toString());
		return tag;
	}
	protected abstract void saveAdditional(@Nonnull CompoundTag tag);

	private static class ItemIcon extends IconData
	{
		private static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"item");

		private final ItemStack iconStack;
		private final String countTextOverride;
		private ItemIcon(ItemStack iconStack, @Nullable String countTextOverride) { super(TYPE); this.iconStack = iconStack; this.countTextOverride = countTextOverride; }

		@Override
		@OnlyIn(Dist.CLIENT)
		public void render(@Nonnull EasyGuiGraphics gui, int x, int y) { gui.renderItem(this.iconStack, x, y, this.countTextOverride); }

		@Override
		protected void saveAdditional(@Nonnull CompoundTag tag) {
			tag.put("Item", this.iconStack.save(new CompoundTag()));
			if(this.countTextOverride != null)
				tag.putString("Text", this.countTextOverride);
		}

		private static IconData loadItem(@Nonnull CompoundTag tag)
		{
			ItemStack stack = ItemStack.of(tag.getCompound("Item"));
			String countText = null;
			if(tag.contains("Text"))
				countText = tag.getString("Text");
			return new ItemIcon(stack,countText);
		}

	}

	private static class ImageIcon extends IconData
	{
		private static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"sprite");

		private final Sprite sprite;
		private ImageIcon(Sprite sprite) { super(TYPE); this.sprite = sprite; }

		@Override
		@OnlyIn(Dist.CLIENT)
		public void render(@Nonnull EasyGuiGraphics gui, int x, int y) { gui.blitSprite(this.sprite, x, y); }

		@Override
		protected void saveAdditional(@Nonnull CompoundTag tag) {
			tag.putString("Image",this.sprite.image.toString());
			tag.putInt("u",this.sprite.u);
			tag.putInt("v",this.sprite.v);
			tag.putInt("w",this.sprite.width);
			tag.putInt("h",this.sprite.height);
			tag.putInt("hou",this.sprite.hoverOffsetU);
			tag.putInt("hov",this.sprite.hoverOffsetV);
		}

		private static IconData loadImage(@Nonnull CompoundTag tag) {
			ResourceLocation image = new ResourceLocation(tag.getString("Image"));
			int u = tag.getInt("u");
			int v = tag.getInt("v");
			int w = tag.getInt("w");
			int h = tag.getInt("h");
			int hou = tag.getInt("hou");
			int hov = tag.getInt("hov");
			return new ImageIcon(new Sprite(image,u,v,w,h,hou,hov));
		}

	}

	private static class TextIcon extends IconData
	{
		private static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"text");

		private final Component iconText;
		private final int textColor;
		private TextIcon(Component iconText, int textColor) {
			super(TYPE);
			this.iconText = iconText;
			this.textColor = textColor;
		}

		@Override
		public void render(@Nonnull EasyGuiGraphics gui, int x, int y)
		{
			int xPos = x + 8 - (gui.font.width(this.iconText)/2);
			int yPos = y + ((16 - gui.font.lineHeight) / 2);
			gui.drawShadowed(this.iconText, xPos, yPos, this.textColor);
		}

		@Override
		protected void saveAdditional(@Nonnull CompoundTag tag) {
			tag.putString("Text", Component.Serializer.toJson(this.iconText));
			tag.putInt("Color",this.textColor);
		}

		private static IconData loadText(@Nonnull CompoundTag tag) {
			Component text = Component.Serializer.fromJson(tag.getString("Text"));
			int color = tag.getInt("Color");
			return new TextIcon(text,color);
		}
	}

	private static class MultiIcon extends IconData
	{
		private static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"multi_icon");
		private final List<IconData> icons;
		private MultiIcon(List<IconData> icons) { super(TYPE); this.icons = icons; }
		@Override
		public void render(@Nonnull EasyGuiGraphics gui, int x, int y) {
			for(IconData icon : this.icons)
				icon.render(gui, x, y);
		}

		@Override
		protected void saveAdditional(@Nonnull CompoundTag tag) {
			ListTag children = new ListTag();
			for(IconData icon : this.icons)
				children.add(icon.save());
			tag.put("Children",children);
		}

		private static IconData loadMulti(@Nonnull CompoundTag tag) {
			List<IconData> result = new ArrayList<>();
			ListTag children = tag.getList("Children", Tag.TAG_COMPOUND);
			for(int i = 0; i < children.size(); ++i)
			{
				IconData icon = load(children.getCompound(i));
				if(icon != null)
					result.add(icon);
			}
			return new MultiIcon(result);
		}

	}

	private static class NullIcon extends IconData
	{
		private static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"null");
		private NullIcon() { super(TYPE); }
		@Override
		public void render(@Nonnull EasyGuiGraphics gui, int x, int y) {}
		@Override
		protected void saveAdditional(@Nonnull CompoundTag tag) { }
	}

	public static IconData of(@Nonnull ItemLike item) { return of(new ItemStack(item)); }
	public static IconData of(@Nonnull ItemLike item, @Nullable String countTextOverride) { return of(new ItemStack(item),countTextOverride); }
	public static IconData of(@Nonnull Supplier<? extends ItemLike> item) { return of(new ItemStack(item.get())); }
	public static IconData of(@Nonnull Supplier<? extends ItemLike> item, @Nullable String countTextOverride) { return of(new ItemStack(item.get()),countTextOverride); }
	public static IconData of(@Nonnull ItemStack iconStack) { return of(iconStack,null); }
	public static IconData of(@Nonnull ItemStack iconStack, @Nullable String countTextOverride) { return iconStack.isEmpty() ? Null() : new ItemIcon(iconStack,countTextOverride); }
	public static IconData of(@Nonnull ResourceLocation iconImage, int u, int v) { return new ImageIcon(Sprite.SimpleSprite(iconImage, u, v, 16, 16)); }
	public static IconData of(@Nonnull Sprite sprite) { return new ImageIcon(sprite); }
	public static IconData of(@Nonnull Component iconText) { return new TextIcon(iconText, 0xFFFFFF); }
	public static IconData of(@Nonnull Component iconText, int textColor) { return new TextIcon(iconText, textColor); }
	public static IconData of(@Nonnull IconData... icons) { return new MultiIcon(Lists.newArrayList(icons)); }

}