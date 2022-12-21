package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TabButton extends Button{
	
	public static final ResourceLocation GUI_TEXTURE = TraderSettingsScreen.GUI_TEXTURE;
	
	public static final int SIZE = 25;
	
	public final ITab tab;
	private final Font font;
	
	private int rotation = 0;
	
	public TabButton(OnPress pressable, Font font, ITab tab)
	{
		super(0, 0, SIZE, SIZE, Component.empty(), pressable, Button.DEFAULT_NARRATION);
		this.font = font;
		this.tab = tab;
	}
	
	public void reposition(int x, int y, int rotation)
	{
		this.setPosition(x, y);
		this.rotation = MathUtil.clamp(rotation, 0, 3);
	}
	
	@Override
	public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		//Set the texture & color for the button
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        float r = (float)(this.getColor() >> 16 & 255) / 255f;
        float g = (float)(this.getColor() >> 8 & 255) / 255f;
        float b = (float)(this.getColor()& 255) / 255f;
        float activeColor = this.active ? 1f : 0.5f;
        RenderSystem.setShaderColor(r * activeColor, g * activeColor, b * activeColor, 1f);
        int xOffset = this.rotation < 2 ? 0 : this.width;
        int yOffset = (this.rotation % 2 == 0 ? 0 : 2 * this.height) + (this.active ? 0 : this.height);
        //Render the background
        this.blit(pose, this.getX(), this.getY(), 200 + xOffset, yOffset, this.width, this.height);
        
        RenderSystem.setShaderColor(activeColor, activeColor, activeColor, 1f);
        this.tab.getIcon().render(pose, this, this.font, this.getX() + 4, this.getY() + 4);
		
	}
	
	protected int getColor() { return this.tab.getColor(); }
	
	public void renderTooltip(PoseStack pose, int mouseX, int mouseY, Screen screen) {
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
		MutableComponent getTooltip();
		
	}

}
