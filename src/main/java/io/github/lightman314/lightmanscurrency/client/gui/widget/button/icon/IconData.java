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

public class IconData {

	private ItemStack iconItem;
	private ResourceLocation iconImage;
	private int iconImageU;
	private int iconImageV;
	private ITextComponent iconText = null;
	private int textColor;
	
	private IconData(ItemStack iconItem) { this.iconItem = iconItem; }
	private IconData(ResourceLocation iconImage, int u, int v)
	{
		this.iconImage = iconImage;
		this.iconImageU = u;
		this.iconImageV = v;
	}
	private IconData(ITextComponent iconText, int textColor) {
		this.iconText = iconText;
		this.textColor = textColor;
	}
	
	public void render(MatrixStack matrixStack, Widget widget, FontRenderer font, int x, int y)
	{
		if(this.iconItem != null)
		{
			ItemRenderUtil.drawItemStack(widget, font, this.iconItem, x, y, false);
		}
		else if(this.iconImage != null)
		{
			Minecraft.getInstance().getTextureManager().bindTexture(this.iconImage);
			widget.blit(matrixStack, x, y, iconImageU, iconImageV, 16, 16);
		}
		else if(this.iconText != null)
		{
			int xPos = x + 8 - (font.getStringWidth(iconText.getString())/2);
			int yPos = y + ((16 - font.FONT_HEIGHT) / 2);
			font.drawStringWithShadow(matrixStack, this.iconText.getString(), xPos, yPos, this.textColor);
		}
	}
	
	public static IconData of(IItemProvider item) { return of(new ItemStack(item)); }
	public static IconData of(ItemStack iconItem) { return new IconData(iconItem); }
	public static IconData of(ResourceLocation iconImage, int u, int v) { return new IconData(iconImage, u,v); }
	public static IconData of(ITextComponent iconText) { return new IconData(iconText, 0xFFFFFF); }
	public static IconData of(ITextComponent iconText, int textColor) { return new IconData(iconText, textColor); }
	
}
