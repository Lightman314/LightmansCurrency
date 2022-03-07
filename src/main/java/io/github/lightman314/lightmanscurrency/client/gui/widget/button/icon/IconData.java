package io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class IconData {

	public abstract void render(MatrixStack matrix, Widget widget, FontRenderer font, int x, int y);
	
	private static class ItemIcon extends IconData
	{
		private final ItemStack iconStack;
		private ItemIcon(ItemStack iconStack) { this.iconStack = iconStack; }
		
		@Override
		public void render(MatrixStack matrix, Widget widget, FontRenderer font, int x, int y) {
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
		public void render(MatrixStack matrix, Widget widget, FontRenderer font, int x, int y) {
			Minecraft.getInstance().getTextureManager().bindTexture(this.iconImage);
			widget.blit(matrix, x, y, this.iconImageU, this.iconImageV, 16, 16);
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
		public void render(MatrixStack matrix, Widget widget, FontRenderer font, int x, int y) {
			int xPos = x + 8 - (font.getStringWidth(iconText.getString())/2);
			int yPos = y + ((16 - font.FONT_HEIGHT) / 2);
			font.drawStringWithShadow(matrix, this.iconText.getString(), xPos, yPos, this.textColor);
		}
		
	}
	
	public static IconData of(IItemProvider item) { return of(new ItemStack(item)); }
	public static IconData of(ItemStack iconItem) { return new ItemIcon(iconItem); }
	public static IconData of(ResourceLocation iconImage, int u, int v) { return new ImageIcon(iconImage, u,v); }
	public static IconData of(ITextComponent iconText) { return new TextIcon(iconText, 0xFFFFFF); }
	public static IconData of(ITextComponent iconText, int textColor) { return new TextIcon(iconText, textColor); }
	
}
