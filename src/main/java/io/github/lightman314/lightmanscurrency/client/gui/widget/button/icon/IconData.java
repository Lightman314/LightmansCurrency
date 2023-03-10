package io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;

public abstract class IconData {
	
	@OnlyIn(Dist.CLIENT)
	public abstract void render(MatrixStack pose, AbstractGui widget, FontRenderer font, int x, int y);
	
	private static class ItemIcon extends IconData
	{
		private final ItemStack iconStack;
		private ItemIcon(ItemStack iconStack) { this.iconStack = iconStack; }
		
		@Override
		public void render(MatrixStack matrixStack, AbstractGui widget, FontRenderer font, int x, int y)
		{
			ItemRenderUtil.drawItemStack(widget, font, this.iconStack, x, y);
		}
		
	}
	
	private static class ImageIcon extends IconData
	{
		private final ResourceLocation iconImage;
		private final int iconImageU;
		private final int iconImageV;
		private ImageIcon(ResourceLocation iconImage, int u, int v) {
			this.iconImage = iconImage;
			this.iconImageU = u;
			this.iconImageV = v;
		}
		
		@Override
		public void render(MatrixStack matrixStack, AbstractGui widget, FontRenderer font, int x, int y)
		{
			RenderUtil.bindTexture(this.iconImage);
			RenderUtil.color4f(1f,1f,1f,1f);
			widget.blit(matrixStack, x, y, iconImageU, iconImageV, 16, 16);
		}
		
	}
	
	private static class TextIcon extends IconData
	{
		private final ITextComponent iconText;
		private final int textColor;
		private TextIcon(ITextComponent iconText, int textColor) {
			this.iconText = iconText;
			this.textColor = textColor;
		}
		
		@Override
		public void render(MatrixStack matrixStack, AbstractGui widget, FontRenderer font, int x, int y)
		{
			int xPos = x + 8 - (font.width(iconText.getString())/2);
			int yPos = y + ((16 - font.lineHeight) / 2);
			font.drawShadow(matrixStack, this.iconText.getString(), xPos, yPos, this.textColor);
		}
	}
	
	private static class MultiIcon extends IconData
	{
		private final List<IconData> icons;
		private MultiIcon(List<IconData> icons) { this.icons = icons; }
		@Override
		public void render(MatrixStack pose, AbstractGui widget, FontRenderer font, int x, int y) {
			for(IconData icon : this.icons)
				icon.render(pose, widget, font, x, y);
		}
	}
	
	public static final IconData BLANK = new IconData() { public void render(MatrixStack pose, AbstractGui widget, FontRenderer font, int x, int y) {} };
	
	public static IconData of(RegistryObject<? extends IItemProvider> item) { return of(item.get()); }
	public static IconData of(IItemProvider item) { return of(new ItemStack(item)); }
	public static IconData of(ItemStack iconStack) { return new ItemIcon(iconStack); }
	public static IconData of(ResourceLocation iconImage, int u, int v) { return new ImageIcon(iconImage, u,v); }
	public static IconData of(ITextComponent iconText) { return new TextIcon(iconText, 0xFFFFFF); }
	public static IconData of(ITextComponent iconText, int textColor) { return new TextIcon(iconText, textColor); }
	public static IconData of(IconData... icons) { return new MultiIcon(Lists.newArrayList(icons)); }
	
}
