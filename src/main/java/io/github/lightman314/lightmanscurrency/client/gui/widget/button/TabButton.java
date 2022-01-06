package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabButton extends Button{
	
	public static final ResourceLocation GUI_TEXTURE = TraderSettingsScreen.GUI_TEXTURE;
	
	public final SettingsTab tab;
	private final Font font;
	
	private int rotation = 0;
	
	public TabButton(OnPress pressable, Font font, SettingsTab tab)
	{
		super(0, 0, 25, 25, new TextComponent(""), pressable);
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
	public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		//Set the texture & color for the button
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        float r = (float)(this.tab.getColor() >> 16 & 255) / 255f;
        float g = (float)(this.tab.getColor() >> 8 & 255) / 255f;
        float b = (float)(this.tab.getColor()& 255) / 255f;
        float activeColor = this.active ? 1f : 0.5f;
        RenderSystem.setShaderColor(r * activeColor, g * activeColor, b * activeColor, 1f);
        int xOffset = this.rotation < 2 ? 0 : this.width;
        int yOffset = (this.rotation % 2 == 0 ? 0 : 2 * this.height) + (this.active ? 0 : this.height);
        //Render the background
        this.blit(matrixStack, x, y, 200 + xOffset, yOffset, this.width, this.height);
        
        RenderSystem.setShaderColor(activeColor, activeColor, activeColor, 1f);
        this.tab.getIcon().render(matrixStack, this, this.font, this.x + 4, this.y + 4);
		
	}

}
