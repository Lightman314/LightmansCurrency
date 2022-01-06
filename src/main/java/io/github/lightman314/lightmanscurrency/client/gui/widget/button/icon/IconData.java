package io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class IconData {

	private ItemStack iconItem;
	private ResourceLocation iconImage;
	private int iconImageU;
	private int iconImageV;
	private Component iconText = null;
	private int textColor;
	
	private IconData(ItemStack iconItem) { this.iconItem = iconItem; }
	private IconData(ResourceLocation iconImage, int u, int v)
	{
		this.iconImage = iconImage;
		this.iconImageU = u;
		this.iconImageV = v;
	}
	private IconData(Component iconText, int textColor) {
		this.iconText = iconText;
		this.textColor = textColor;
	}
	
	public void render(PoseStack matrixStack, AbstractWidget widget, Font font, int x, int y)
	{
		if(this.iconItem != null)
		{
			ItemRenderUtil.drawItemStack(font, this.iconItem, x, y, false);
		}
		else if(this.iconImage != null)
		{
			RenderSystem.setShaderTexture(0, this.iconImage);
			widget.blit(matrixStack, x, y, iconImageU, iconImageV, 16, 16);
		}
		else if(this.iconText != null)
		{
			int xPos = x + 8 - (font.width(iconText.getString())/2);
			int yPos = y + ((16 - font.lineHeight) / 2);
			font.drawShadow(matrixStack, this.iconText.getString(), xPos, yPos, this.textColor);
		}
	}
	
	public static IconData of(ItemLike item) { return of(new ItemStack(item)); }
	public static IconData of(ItemStack iconItem) { return new IconData(iconItem); }
	public static IconData of(ResourceLocation iconImage, int u, int v) { return new IconData(iconImage, u,v); }
	public static IconData of(Component iconText) { return new IconData(iconText, 0xFFFFFF); }
	public static IconData of(Component iconText, int textColor) { return new IconData(iconText, textColor); }
	
}
