package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IconButton extends Button{
	
	private IconData icon;
	private FontRenderer font;
	
	public IconButton(int x, int y, IPressable pressable, FontRenderer font, IconData icon)
	{
		super(x,y,20,20, new StringTextComponent(""), pressable);
		this.setIcon(icon);
		this.font = font;
	}
	@SuppressWarnings("resource")
	@Deprecated
	public IconButton(int x, int y, IPressable pressable, ResourceLocation iconResource, int resourceX, int resourceY)
	{
		super(x,y,20,20, new StringTextComponent(""), pressable);
		this.setIcon(IconData.of(iconResource, resourceX, resourceY));
		this.font = Minecraft.getInstance().fontRenderer;
	}
	
	@Deprecated //Use setIcon instead
	public void setResource(ResourceLocation iconResource, int resourceX, int resourceY)
	{
		this.icon = IconData.of(iconResource, resourceX, resourceY);
	}
	
	public void setIcon(IconData icon)
	{
		this.icon = icon;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft.getInstance().getTextureManager().bindTexture(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int offset = this.getYImage(this.isHovered());
        this.blit(matrixStack, this.x, this.y, 0, 46 + offset * 20, this.width / 2, this.height);
        this.blit(matrixStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + offset * 20, this.width / 2, this.height);
        if(!this.active)
            RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
        
        this.icon.render(matrixStack, this, this.font, this.x + 2, this.y + 2);
		
	}

}
