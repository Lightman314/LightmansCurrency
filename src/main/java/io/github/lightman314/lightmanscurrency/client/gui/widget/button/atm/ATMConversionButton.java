package io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.common.atm.ATMConversionButtonData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ATMConversionButton extends Button {
	
	public static final int HEIGHT = 18;
	
	private final ATMConversionButtonData data;
	
	public ATMConversionButton(int left, int top, ATMConversionButtonData data, Consumer<String> commandHandler) {
		super(left + data.xPos, top + data.yPos, data.width, HEIGHT, Component.empty(), b -> commandHandler.accept(data.command));
		this.data = data;
	}
	
	@Override
	public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		//Render background to width
		int yOffset = this.isHovered ? HEIGHT : 0;
		RenderSystem.setShaderTexture(0, ATMScreen.BUTTON_TEXTURE);
		if(this.active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		//Draw the left edge
		this.blit(pose, this.x, this.y, 0, yOffset, 2, HEIGHT);
		//Draw the middle portions
		int xPos = 2;
		while(xPos < this.width - 2)
		{
			int xSize = Math.min(this.width - 2 - xPos, 252);
			this.blit(pose, this.x + xPos, this.y, 2, yOffset, xSize, HEIGHT);
			xPos += xSize;
		}
		//Draw the right edge
		this.blit(pose, this.x + this.width - 2, this.y, 254, yOffset, 2, HEIGHT);
		
		//Draw the icons
		for(ATMIconData icon : this.data.getIcons())
		{
			try {
				icon.render(this, pose, this.isHovered);
			} catch(Exception e) { LightmansCurrency.LogError("Error rendering ATM Conversion Button icon.", e); }
		}
		
	}
	
}
