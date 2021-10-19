package io.github.lightman314.lightmanscurrency.client.gui.util;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

public class ItemRenderUtil {

	/**
    * Draws an ItemStack.
    *  
    * The z index is increased by 32 (and not decreased afterwards), and the item is then rendered at z=200.
    */
	@SuppressWarnings("deprecation")
	public static void drawItemStack(Widget widget, @Nullable FontRenderer font, ItemStack stack, int x, int y, boolean drawCount) {
		
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
	   
      	RenderSystem.translatef(0.0F, 0.0F, 32.0F);
      	widget.setBlitOffset(200);
      	itemRenderer.zLevel = 200.0F;
      	itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
      	font = stack.getItem().getFontRenderer(stack) != null ? stack.getItem().getFontRenderer(stack) : font;
      	if(drawCount && font != null)
      		itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, (String)null);
      	widget.setBlitOffset(0);
      	itemRenderer.zLevel = 0.0F;
   	}
	
	/**
    * Draws an ItemStack.
    *  
    * The z index is increased by 32 (and not decreased afterwards), and the item is then rendered at z=200.
    */
	@SuppressWarnings("deprecation")
	public static void drawItemStack(Screen screen, @Nullable FontRenderer font, ItemStack stack, int x, int y, boolean drawCount) {
		
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
	   
      	RenderSystem.translatef(0.0F, 0.0F, 32.0F);
      	screen.setBlitOffset(200);
      	itemRenderer.zLevel = 200.0F;
      	itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
      	font = stack.getItem().getFontRenderer(stack) != null ? stack.getItem().getFontRenderer(stack) : font;
      	if(drawCount && font != null)
      		itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, (String)null);
      	screen.setBlitOffset(0);
      	itemRenderer.zLevel = 0.0F;
   	}
	
}
