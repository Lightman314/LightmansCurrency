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
		super(x , y, width, DropdownWidget.HEIGHT, Component.empty(), pressable);
		this.optionText = optionText;
		this.font = font;
	}
	
	@Override
	public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		//Draw the background
		RenderSystem.setShaderTexture(0, DropdownWidget.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int offset = (this.isHovered ? this.height : 0) + (DropdownWidget.HEIGHT * 2);
        if(!this.active)
        	RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        this.blit(pose, this.x, this.y, 0, offset, 2, DropdownWidget.HEIGHT);
        int xOffset = 0;
        while(xOffset < this.width - 4)
        {
        	int xPart = Math.min(this.width - 4 - xOffset, 252);
        	this.blit(pose, this.x + 2 + xOffset, this.y, 2, offset, xPart, DropdownWidget.HEIGHT);
        	xOffset += xPart;
        }
        this.blit(pose, this.x + this.width - 2, this.y, 254, offset, 2, DropdownWidget.HEIGHT);
        //Draw the option text
        this.font.draw(pose, TextRenderUtil.fitString(this.optionText, this.width - 4), this.x + 2, this.y + 2, 0x404040);
        
	}

}
