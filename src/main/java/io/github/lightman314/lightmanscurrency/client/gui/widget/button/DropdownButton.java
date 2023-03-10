package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class DropdownButton extends Button {
	
	private final ITextComponent optionText;
	private final FontRenderer font;
	
	public DropdownButton(int x, int y, int width, FontRenderer font, ITextComponent optionText, IPressable pressable)
	{
		super(x , y, width, DropdownWidget.HEIGHT, EasyText.empty(), pressable);
		this.optionText = optionText;
		this.font = font;
	}
	
	@Override
	public void renderButton(@Nonnull MatrixStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		//Draw the background
		RenderUtil.bindTexture(DropdownWidget.GUI_TEXTURE);
		RenderUtil.color4f(1f, 1f, 1f, 1f);
        int offset = (this.isHovered ? this.height : 0) + (DropdownWidget.HEIGHT * 2);
        if(!this.active)
			RenderUtil.color4f(0.5F, 0.5F, 0.5F, 1.0F);
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
        this.font.draw(poseStack, TextRenderUtil.fitString(this.optionText, this.width - 4), this.x + 2, this.y + 2, 0x404040);
        
	}

}
