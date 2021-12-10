package io.github.lightman314.lightmanscurrency.client.util;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemRenderUtil {

	public static final int ITEM_BLIT_OFFSET = 200;
	
	
	/**
    * Draws an ItemStack.
    *  
    * The z index is increased by 32 (and not decreased afterwards), and the item is then rendered at z=200.
    */
	public static void drawItemStack(@Nullable Font font, ItemStack stack, int x, int y, boolean drawCount) {
		
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
	   
      	itemRenderer.blitOffset = 200.0F;
      	itemRenderer.renderGuiItem(stack, x, y);
      	if(drawCount && font != null)
      		itemRenderer.renderGuiItemDecorations(font, stack, x, y);
      	itemRenderer.blitOffset = 0.0F;
   	}
	
	/**
    * Draws an ItemStack.
    *  
    * The z index is increased by 32 (and not decreased afterwards), and the item is then rendered at z=200.
    */
	public static void drawItemStack(Screen screen, @Nullable Font font, ItemStack stack, int x, int y, boolean drawCount) {
		
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
	   
      	//RenderSystem.translatef(0.0F, 0.0F, 32.0F);
      	screen.setBlitOffset(200);
      	itemRenderer.blitOffset = 200.0F;
      	itemRenderer.renderGuiItem(stack, x, y);
      	//font = stack.getItem().getFontRenderer(stack) != null ? stack.getItem().getFontRenderer(stack) : font;
      	if(drawCount && font != null)
      		itemRenderer.renderGuiItemDecorations(font, stack, x, y);
      	screen.setBlitOffset(0);
      	itemRenderer.blitOffset = 0.0F;
   	}
	
	/**
	 * Renders an item slots background
	 */
	public static void drawSlotBackground(PoseStack matrixStack, int x, int y, Pair<ResourceLocation,ResourceLocation> background)
	{
		Minecraft minecraft = Minecraft.getInstance();
		TextureAtlasSprite textureatlassprite = minecraft.getTextureAtlas(background.getFirst()).apply(background.getSecond());
		RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
        Screen.blit(matrixStack, x, y, 100, 16, 16, textureatlassprite);
	}
	
}
