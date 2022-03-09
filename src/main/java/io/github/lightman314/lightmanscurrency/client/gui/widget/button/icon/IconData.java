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

public abstract class IconData {

	public abstract void render(PoseStack pose, AbstractWidget widget, Font font, int x, int y);
	
	private static class ItemIcon extends IconData
	{
		private final ItemStack iconStack;
		private ItemIcon(ItemStack iconStack) { this.iconStack = iconStack; }
		
		@Override
		public void render(PoseStack matrixStack, AbstractWidget widget, Font font, int x, int y)
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
		public void render(PoseStack matrixStack, AbstractWidget widget, Font font, int x, int y)
		{
			RenderSystem.setShaderTexture(0, this.iconImage);
			widget.blit(matrixStack, x, y, iconImageU, iconImageV, 16, 16);
		}
		
	}
	
	private static class TextIcon extends IconData
	{
		private final Component iconText;
		private final int textColor;
		private TextIcon(Component iconText, int textColor) {
			this.iconText = iconText;
			this.textColor = textColor;
		}
		
		@Override
		public void render(PoseStack matrixStack, AbstractWidget widget, Font font, int x, int y)
		{
			int xPos = x + 8 - (font.width(iconText.getString())/2);
			int yPos = y + ((16 - font.lineHeight) / 2);
			font.drawShadow(matrixStack, this.iconText.getString(), xPos, yPos, this.textColor);
		}
	}
	
	public static IconData of(ItemLike item) { return of(new ItemStack(item)); }
	public static IconData of(ItemStack iconStack) { return new ItemIcon(iconStack); }
	public static IconData of(ResourceLocation iconImage, int u, int v) { return new ImageIcon(iconImage, u,v); }
	public static IconData of(Component iconText) { return new TextIcon(iconText, 0xFFFFFF); }
	public static IconData of(Component iconText, int textColor) { return new TextIcon(iconText, textColor); }
	
}
