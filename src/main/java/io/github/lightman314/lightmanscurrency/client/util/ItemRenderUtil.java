package io.github.lightman314.lightmanscurrency.client.util;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;

public class ItemRenderUtil {

	public static final int ITEM_BLIT_OFFSET = 200;
	
	private static ItemStack alexHead = null;
	
	public static ItemStack getAlexHead()
	{
		if(alexHead != null)
			return alexHead;
		ItemStack alexHead = new ItemStack(Items.PLAYER_HEAD);
		CompoundNBT headData = new CompoundNBT();
		CompoundNBT skullOwner = new CompoundNBT();
		skullOwner.putIntArray("Id", new int[] {-731408145, -304985227, -1778597514, 158507129 });
		CompoundNBT properties = new CompoundNBT();
		ListNBT textureList = new ListNBT();
		CompoundNBT texture = new CompoundNBT();
		texture.putString("Value", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNiMDk4OTY3MzQwZGFhYzUyOTI5M2MyNGUwNDkxMDUwOWIyMDhlN2I5NDU2M2MzZWYzMWRlYzdiMzc1MCJ9fX0=");
		textureList.add(texture);
		properties.put("textures", textureList);
		skullOwner.put("Properties", properties);
		headData.put("SkullOwner", skullOwner);
		alexHead.setTag(headData);
		return alexHead;
	}
	
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
	
	/**
	 * Renders an item slots background
	 */
	public static void drawSlotBackground(MatrixStack matrixStack, int x, int y, Pair<ResourceLocation,ResourceLocation> background)
	{
		Minecraft minecraft = Minecraft.getInstance();
		TextureAtlasSprite textureatlassprite = minecraft.getAtlasSpriteGetter(background.getFirst()).apply(background.getSecond());
		minecraft.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
        Screen.blit(matrixStack, x, y, 100, 16, 16, textureatlassprite);
	}
	
}
