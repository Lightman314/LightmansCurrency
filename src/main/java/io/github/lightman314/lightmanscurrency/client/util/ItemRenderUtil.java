package io.github.lightman314.lightmanscurrency.client.util;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.gui.widget.util.IScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class ItemRenderUtil extends GuiComponent{
	
	private static ItemStack alexHead = null;
	
	public static ItemStack getAlexHead()
	{
		if(alexHead != null)
			return alexHead;
		alexHead = new ItemStack(Items.PLAYER_HEAD);
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
	 */
	public static void drawItemStack(PoseStack pose, Font font, ItemStack stack, int x, int y) { drawItemStack(pose, font, stack, x, y, null); }
	
	/**
    * Draws an ItemStack.
    */
	public static void drawItemStack(PoseStack pose, Font font, ItemStack stack, int x, int y, @Nullable String customCount) {
		
		Minecraft minecraft = Minecraft.getInstance();
		
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		Screen screen = minecraft.screen;
		int imageWidth = 0;
		if(screen != null)
		{
			imageWidth = screen.width;
			if(screen instanceof AbstractContainerScreen<?> s)
				imageWidth = s.getXSize();
			else if(screen instanceof IScreen s)
				imageWidth = s.getXSize();
		}
		
		if(font == null)
			font = minecraft.font;
		
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		itemRenderer.renderAndDecorateItem(pose, stack, x, y, x + y * imageWidth);
        itemRenderer.renderGuiItemDecorations(pose, font, stack, x, y, customCount);
        
   	}
	
	/**
	 * Renders an item slots background
	 */
	public static void drawSlotBackground(PoseStack matrixStack, int x, int y, Pair<ResourceLocation,ResourceLocation> background)
	{
		if(background == null)
			return;
		Minecraft minecraft = Minecraft.getInstance();
		TextureAtlasSprite textureatlassprite = minecraft.getTextureAtlas(background.getFirst()).apply(background.getSecond());
		RenderSystem.setShaderColor(1f,1f,1f,1f);
		RenderSystem.setShaderTexture(0, textureatlassprite.atlasLocation());
        blit(matrixStack, x, y, 100, 16, 16, textureatlassprite);
	}
	
	/**
	 * Gets the tooltip for an item stack
	 */
	public static List<Component> getTooltipFromItem(ItemStack stack) {
		Minecraft minecraft = Minecraft.getInstance();
		return stack.getTooltipLines(minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
	}

	/**
	 * Translates the pose so that textures will be rendered in front of items.
	 * Recommended to run pose.pushPose(), and then run pose.popPose() after
	 * you are done rendering what you desire to be drawn in front.
	 */
	public static void TranslateToForeground(PoseStack pose) { pose.translate(0d, 0d, 250d);}
	
}
