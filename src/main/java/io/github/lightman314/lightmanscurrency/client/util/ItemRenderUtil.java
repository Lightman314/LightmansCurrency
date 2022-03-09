package io.github.lightman314.lightmanscurrency.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemRenderUtil {

	public static final int ITEM_BLIT_OFFSET = 200;
	
	private static ItemStack alexHead = null;
	
	public static ItemStack getAlexHead()
	{
		if(alexHead != null)
			return alexHead;
		ItemStack alexHead = new ItemStack(Items.PLAYER_HEAD);
		CompoundTag headData = new CompoundTag();
		CompoundTag skullOwner = new CompoundTag();
		skullOwner.putIntArray("Id", new int[] {-731408145, -304985227, -1778597514, 158507129 });
		CompoundTag properties = new CompoundTag();
		ListTag textureList = new ListTag();
		CompoundTag texture = new CompoundTag();
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
	public static void drawItemStack(GuiComponent gui, Font font, ItemStack stack, int x, int y) {
		
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		Player player = minecraft.player;
		Screen screen = minecraft.screen;
		
		int imageWidth = 0;
		if(screen != null)
		{
			imageWidth = screen.width;
			if(screen instanceof AbstractContainerScreen<?>)
				imageWidth = ((AbstractContainerScreen<?>)screen).getXSize();
		}
		   
		gui.setBlitOffset(100);
		itemRenderer.blitOffset = 100.0F;

		RenderSystem.enableDepthTest();

        itemRenderer.renderAndDecorateItem(player, stack, x, y, x + y * imageWidth);
        itemRenderer.renderGuiItemDecorations(font, stack, x, y, null);

        itemRenderer.blitOffset = 0.0F;
        gui.setBlitOffset(0);
        
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
