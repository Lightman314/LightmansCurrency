package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DropdownButton extends Button{
	
	private final Component optionText;
	private final Font font;
	
	public DropdownButton(int x, int y, int width, Font font, Component optionText, OnPress pressable)
	{
		super(x , y, width, DropdownWidget.HEIGHT, new TextComponent(""), pressable);
		this.optionText = optionText;
		this.font = font;
	}
	
	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		//Draw the background
		RenderSystem.setShaderTexture(0, DropdownWidget.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int offset = (this.isHovered ? this.height : 0) + (DropdownWidget.HEIGHT * 2);
        if(!this.active)
        	RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        this.blit(poseStack, this.x, this.y, 0, offset, 2, DropdownWidget.HEIGHT);
        int xOffset = 0;
        while(xOffset < this.width - 4)
        {
        	int xPart = Math.min(this.width - 4 - xOffset, 252);
        	this.blit(poseStack, this.x + 2 + xOffset, this.y, 2, offset, xPart, DropdownWidget.HEIGHT);
        	xOffset += xPart;
        }
        this.blit(poseStack, this.x + this.width - 2, this.y, 254, offset, 2, DropdownWidget.HEIGHT);
        //Draw the option text
        this.font.draw(poseStack, this.fitString(this.optionText.getString()), this.x + 2, this.y + 2, 0x404040);
        
	}
	
	private String fitString(String text) {
		if(this.font.width(text) <= this.width - 4)
			return text;
		while(this.font.width(text + "...") <= this.width - 4 && text.length() > 0)
		{
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

}
