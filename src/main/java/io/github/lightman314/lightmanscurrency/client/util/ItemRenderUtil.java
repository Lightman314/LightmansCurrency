package io.github.lightman314.lightmanscurrency.client.util;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class ItemRenderUtil {

	public static final int ITEM_BLIT_OFFSET = 100;
	
	private static ItemStack alexHead = null;
	
	public static ItemStack getAlexHead()
	{
		if(alexHead != null)
			return alexHead;
		alexHead = new ItemStack(Items.PLAYER_HEAD);
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
	 */
	public static void drawItemStack(AbstractGui gui, FontRenderer font, ItemStack stack, int x, int y) { drawItemStack(gui, font, stack, x, y, null); }
	
	/**
    * Draws an ItemStack.
    */
	public static void drawItemStack(AbstractGui gui, FontRenderer font, ItemStack stack, int x, int y, @Nullable String customCount) {
		
		Minecraft minecraft = Minecraft.getInstance();
		
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		PlayerEntity player = minecraft.player;
		
		if(font == null)
			font = minecraft.font;
		
		gui.setBlitOffset(ITEM_BLIT_OFFSET);
		itemRenderer.blitOffset = ITEM_BLIT_OFFSET;
		
		RenderSystem.enableDepthTest();
		
        itemRenderer.renderAndDecorateItem(player, stack, x, y);
        itemRenderer.renderGuiItemDecorations(font, stack, x, y, customCount);
        
        itemRenderer.blitOffset = 0.0F;
        gui.setBlitOffset(0);
        
   	}
	
	/**
	 * Renders an item slots background
	 */
	public static void drawSlotBackground(MatrixStack matrixStack, int x, int y, Pair<ResourceLocation,ResourceLocation> background)
	{
		if(background == null)
			return;
		Minecraft minecraft = Minecraft.getInstance();
		TextureAtlasSprite textureatlassprite = minecraft.getTextureAtlas(background.getFirst()).apply(background.getSecond());
		RenderUtil.color4f(1f,1f,1f,1f);
		RenderUtil.bindTexture(textureatlassprite.atlas().location());
        Screen.blit(matrixStack, x, y, ITEM_BLIT_OFFSET, 16, 16, textureatlassprite);
	}
	
	/**
	 * Gets the tooltip for an item stack
	 */
	public static List<ITextComponent> getTooltipFromItem(ItemStack stack) {
		Minecraft minecraft = Minecraft.getInstance();
		return stack.getTooltipLines(minecraft.player, minecraft.options.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
	}
	
}
