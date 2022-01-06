package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IconButton extends Button{
	
	private IconData icon;
	private Font font;
	
	public IconButton(int x, int y, OnPress pressable, Font font, IconData icon)
	{
		super(x,y,20,20,new TextComponent(""), pressable);
		this.setIcon(icon);
		this.font = font;
	}
	
	@SuppressWarnings("resource")
	@Deprecated
	public IconButton(int x, int y, OnPress pressable, ResourceLocation iconResource, int resourceX, int resourceY)
	{
		super(x,y,20,20, new TextComponent(""), pressable);
		this.setIcon(IconData.of(iconResource, resourceX, resourceY));
		this.font = Minecraft.getInstance().font;
	}
	
	@Deprecated
	public void setResource(ResourceLocation iconResource, int resourceX, int resourceY)
	{
		this.icon = IconData.of(iconResource, resourceX, resourceY);
	}
	
	public void setIcon(IconData icon)
	{
		this.icon = icon;
	}
	
	@Override
	public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		RenderSystem.setShaderColor(1f,  1f,  1f, 1f);
		
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int offset = this.getYImage(this.isHovered);
        this.blit(matrixStack, this.x, this.y, 0, 46 + offset * 20, this.width / 2, this.height);
        this.blit(matrixStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + offset * 20, this.width / 2, this.height);
        if(!this.active)
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        
        this.icon.render(matrixStack, this, this.font, this.x + 2, this.y + 2);
		
	}

}
