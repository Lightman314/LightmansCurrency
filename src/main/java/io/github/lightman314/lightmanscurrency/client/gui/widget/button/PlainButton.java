package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlainButton extends Button{
	
	private ResourceLocation buttonResource;
	private int resourceX;
	private int resourceY;
	
	
	public PlainButton(int x, int y, int sizeX, int sizeY, OnPress pressable, ResourceLocation buttonResource, int resourceX, int resourceY)
	{
		super(x,y,sizeX,sizeY, new TextComponent(""), pressable);
		this.buttonResource = buttonResource;
		this.resourceX = resourceX;
		this.resourceY = resourceY;
	}
	
	public void setResource(ResourceLocation buttonResource, int resourceX, int resourceY)
	{
		this.buttonResource = buttonResource;
		this.resourceX = resourceX;
		this.resourceY = resourceY;
	}
	
	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, this.buttonResource);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int offset = this.isHovered ? this.height : 0;
        if(!this.active)
        	RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        this.blit(poseStack, this.x, this.y, this.resourceX, this.resourceY + offset, this.width, this.height);
		
	}

}
