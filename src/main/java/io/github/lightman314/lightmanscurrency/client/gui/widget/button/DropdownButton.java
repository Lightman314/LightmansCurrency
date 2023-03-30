package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DropdownButton extends Button{
	
	private final Component optionText;
	private final Font font;
	
	public DropdownButton(int x, int y, int width, Font font, Component optionText, OnPress pressable)
	{
		super(x , y, width, DropdownWidget.HEIGHT, Component.empty(), pressable, Button.DEFAULT_NARRATION);
		this.optionText = optionText;
		this.font = font;
	}
	
	@Override
	public void renderWidget(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		//Draw the background
		RenderSystem.setShaderTexture(0, DropdownWidget.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int offset = (this.isHovered ? this.height : 0) + (DropdownWidget.HEIGHT * 2);
        if(!this.active)
        	RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        blit(pose, this.getX(), this.getY(), 0, offset, 2, DropdownWidget.HEIGHT);
        int xOffset = 0;
        while(xOffset < this.width - 4)
        {
        	int xPart = Math.min(this.width - 4 - xOffset, 252);
        	blit(pose, this.getX() + 2 + xOffset, this.getY(), 2, offset, xPart, DropdownWidget.HEIGHT);
        	xOffset += xPart;
        }
        blit(pose, this.getX() + this.width - 2, this.getY(), 254, offset, 2, DropdownWidget.HEIGHT);
        //Draw the option text
        this.font.draw(pose, TextRenderUtil.fitString(this.optionText, this.width - 4), this.getX() + 2, this.getY() + 2, 0x404040);

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

	}

}
