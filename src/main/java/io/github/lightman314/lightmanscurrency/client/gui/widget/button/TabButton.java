package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabButton extends Button {
	
	public static final ResourceLocation GUI_TEXTURE = TraderSettingsScreen.GUI_TEXTURE;
	
	public static final int SIZE = 25;
	
	public final ITab tab;
	private final FontRenderer font;
	
	private int rotation = 0;
	
	public TabButton(IPressable pressable, FontRenderer font, ITab tab)
	{
		super(0, 0, SIZE, SIZE, EasyText.empty(), pressable);
		this.font = font;
		this.tab = tab;
	}
	
	public void reposition(int x, int y, int rotation)
	{
		this.x = x;
		this.y = y;
		this.rotation = MathUtil.clamp(rotation, 0, 3);
	}
	
	@Override
	public void renderButton(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		//Set the texture & color for the button
		Minecraft.getInstance().getTextureManager().bind(GUI_TEXTURE);
        float r = (float)(this.getColor() >> 16 & 255) / 255f;
        float g = (float)(this.getColor() >> 8 & 255) / 255f;
        float b = (float)(this.getColor()& 255) / 255f;
        float activeColor = this.active ? 1f : 0.5f;
        RenderUtil.color4f(r * activeColor, g * activeColor, b * activeColor, 1f);
        int xOffset = this.rotation < 2 ? 0 : this.width;
        int yOffset = (this.rotation % 2 == 0 ? 0 : 2 * this.height) + (this.active ? 0 : this.height);
        //Render the background
        this.blit(matrixStack, x, y, 200 + xOffset, yOffset, this.width, this.height);

		RenderUtil.color4f(activeColor, activeColor, activeColor, 1f);
        this.tab.getIcon().render(matrixStack, this, this.font, this.x + 4, this.y + 4);
		
	}
	
	protected int getColor() { return this.tab.getColor(); }
	
	public void renderTooltip(MatrixStack pose, int mouseX, int mouseY, Screen screen) {
		boolean wasActive = this.active;
		this.active = true;
		if(this.visible && this.isMouseOver(mouseX, mouseY))
			screen.renderTooltip(pose, this.tab.getTooltip(), mouseX, mouseY);
		this.active = wasActive;
	}
	
	public interface ITab
	{
		@Nonnull
		IconData getIcon();
		int getColor();
		ITextComponent getTooltip();
		
	}

}
