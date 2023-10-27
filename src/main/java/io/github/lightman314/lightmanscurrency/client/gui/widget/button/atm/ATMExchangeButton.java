package io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.atm.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;

import javax.annotation.Nonnull;

public class ATMExchangeButton extends EasyButton {
	
	public static final int HEIGHT = 18;
	
	public final ATMExchangeButtonData data;

	public boolean selected = false;
	
	public ATMExchangeButton(ScreenPosition corner, ATMExchangeButtonData data, Consumer<String> commandHandler) {
		super(corner.offset(data.position), data.width, HEIGHT, b -> commandHandler.accept(data.command));
		this.data = data;
	}

	@Override
	public ATMExchangeButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {
		
		//Render background to width
		int yOffset = this.isHovered != this.selected ? HEIGHT : 0;
		if(this.active)
			gui.resetColor();
		else
			gui.setColor(0.5f,0.5f,0.5f);
		//Draw the left edge
		gui.blit(ATMScreen.BUTTON_TEXTURE, 0, 0, 0, yOffset, 2, HEIGHT);
		//Draw the middle portions
		int xPos = 2;
		while(xPos < this.getWidth() - 2)
		{
			int xSize = Math.min(this.getWidth() - 2 - xPos, 252);
			gui.blit(ATMScreen.BUTTON_TEXTURE, xPos, 0, 2, yOffset, xSize, HEIGHT);
			xPos += xSize;
		}
		//Draw the right edge
		gui.blit(ATMScreen.BUTTON_TEXTURE, this.getWidth() - 2, 0, 254, yOffset, 2, HEIGHT);
		
		//Draw the icons
		for(ATMIconData icon : this.data.getIcons())
		{
			try { icon.render(this, gui, this.isHovered);
			} catch(Throwable t) { LightmansCurrency.LogError("Error rendering ATM Conversion Button icon.", t); }
		}

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
	}
	
}
