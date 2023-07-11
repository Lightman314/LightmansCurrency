package io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown;

import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class DropdownButton extends EasyButton {
	
	private final Component optionText;
	
	public DropdownButton(int x, int y, int width, Component optionText, Consumer<EasyButton> pressable)
	{
		super(x , y, width, DropdownWidget.HEIGHT, pressable);
		this.optionText = optionText;
	}

	@Override
	public DropdownButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		//Draw the background
        int offset = (this.isHovered ? this.height : 0) + (DropdownWidget.HEIGHT * 2);
        if(!this.active)
			gui.setColor(0.5f,0.5f,0.5f);
		else
			gui.resetColor();
		gui.blit(DropdownWidget.GUI_TEXTURE, 0, 0, 0, offset, 2, DropdownWidget.HEIGHT);
        int xOffset = 0;
        while(xOffset < this.width - 4)
        {
        	int xPart = Math.min(this.width - 4 - xOffset, 252);
			gui.blit(DropdownWidget.GUI_TEXTURE, 2 + xOffset, 0, 2, offset, xPart, DropdownWidget.HEIGHT);
        	xOffset += xPart;
        }
		gui.blit(DropdownWidget.GUI_TEXTURE, this.width - 2, 0, 254, offset, 2, DropdownWidget.HEIGHT);
        //Draw the option text
		gui.drawString(TextRenderUtil.fitString(this.optionText, this.width - 4), 2, 2, 0x404040);

		gui.resetColor();

	}

}
