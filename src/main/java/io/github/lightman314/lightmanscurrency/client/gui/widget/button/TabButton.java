package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabButton extends Button{
	
	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tradersettings.png");
	
	public final ITab tab;
	private final FontRenderer font;
	
	private int rotation = 0;
	
	public TabButton(IPressable pressable, FontRenderer font, ITab tab)
	{
		super(0, 0, 25, 25, new StringTextComponent(""), pressable);
		this.font = font;
		this.tab = tab;
	}
	
	public void reposition(int x, int y, int rotation)
	{
		this.x = x;
		this.y = y;
		this.rotation = MathUtil.clamp(rotation, 0, 3);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		//Set the texture & color for the button
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
        float r = (float)(this.tab.getColor() >> 16 & 255) / 255f;
        float g = (float)(this.tab.getColor() >> 8 & 255) / 255f;
        float b = (float)(this.tab.getColor()& 255) / 255f;
        float activeColor = this.active ? 1f : 0.5f;
        RenderSystem.color3f(r * activeColor, g * activeColor, b * activeColor);
        int xOffset = this.rotation < 2 ? 0 : this.width;
        int yOffset = (this.rotation % 2 == 0 ? 0 : 2 * this.height) + (this.active ? 0 : this.height);
        //Render the background
        this.blit(matrixStack, x, y, 200 + xOffset, yOffset, this.width, this.height);
        
        RenderSystem.color3f(activeColor, activeColor, activeColor);
        this.tab.getIcon().render(matrixStack, this, this.font, this.x + 4, this.y + 4);
		
	}
	
	public interface ITab
	{
		@Nonnull
		public IconData getIcon();
		public int getColor();
		public ITextComponent getTooltip();
	}

}
