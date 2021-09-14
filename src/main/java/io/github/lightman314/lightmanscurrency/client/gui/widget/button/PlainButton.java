package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
//import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlainButton extends Button{
	
	private ResourceLocation buttonResource;
	private int resourceX;
	private int resourceY;
	
	
	public PlainButton(int x, int y, int sizeX, int sizeY, IPressable pressable, ResourceLocation buttonResource, int resourceX, int resourceY)
	{
		super(x,y,sizeX,sizeY, ITextComponent.getTextComponentOrEmpty(""), pressable);
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
	
	@SuppressWarnings("deprecation")
	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		//Minecraft.getInstance().getTextureManager().bindTexture(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int offset = this.isHovered() ? this.height : 0;
        Minecraft.getInstance().getTextureManager().bindTexture(this.buttonResource);
        if(!this.active)
        	RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
        this.blit(matrixStack, this.x, this.y, this.resourceX, this.resourceY + offset, this.width, this.height);
		
	}

}
