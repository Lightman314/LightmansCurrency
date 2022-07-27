package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class TextLogWindow extends AbstractWidget{

	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/textlog.png");
	
	private final Supplier<TextLogger> logger;
	private final Font font;
	
	int scroll = 0;
	
	//OG 
	public TextLogWindow(int x, int y, Supplier<TextLogger> logger, Font font)
	{
		super(x, y, 256, 256, new TextComponent(""));
		this.logger = logger;
		this.font = font;
	}
	
	@SuppressWarnings("resource")
	public TextLogWindow(AbstractContainerScreen<?> screen, Supplier<TextLogger> logger)
	{
		super(screen.getGuiLeft(), screen.getGuiTop(), screen.getXSize(), screen.getYSize(), new TextComponent(""));
		this.logger = logger;
		this.font = screen.getMinecraft().font;
	}
	
	public TextLogWindow(int x, int y, int width, int height, Font font, Supplier<TextLogger> logger) {
		super(x, y, width, height, new TextComponent(""));
		this.logger = logger;
		this.font = font;
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f,  1f,  1f, 1f);
		//Render the background
		//Render the top edge
		this.blit(poseStack, this.x, this.y, 0, 0, 7, 7);
		int xOffset = 7;
		while(xOffset < this.width - 7)
		{
			int thisWidth = Math.min(256 - 14, this.width - 7 - xOffset);
			this.blit(poseStack, this.x + xOffset, this.y, 7, 0, thisWidth, 7);
			xOffset += thisWidth;
		}
		this.blit(poseStack, this.x + this.width - 7, this.y, 256 - 7, 0, 7, 7);
		//Render the middle
		int yOffset = 7;
		while(yOffset < this.height - 7)
		{
			int thisHeight = Math.min(256 - 14, this.height - 7 - yOffset);
			this.blit(poseStack, this.x, this.y + yOffset, 0, 7, 7, thisHeight);
			xOffset = 7;
			while(xOffset < this.width - 7)
			{
				int thisWidth = Math.min(256 - 14, this.width - 7 - xOffset);
				this.blit(poseStack, this.x + xOffset, this.y + yOffset, 7, 7, thisWidth, thisHeight);
				xOffset += thisWidth;
			}
			this.blit(poseStack, this.x + this.width - 7, this.y + yOffset, 256 - 7, 7, 7, thisHeight);
			yOffset += thisHeight;
		}
		//Render the bottom edge
		this.blit(poseStack, this.x, this.y + this.height - 7, 0, 256 - 7, 7, 7);
		xOffset = 7;
		while(xOffset < this.width - 7)
		{
			int thisWidth = Math.min(256 - 14, this.width - 7 - xOffset);
			this.blit(poseStack, this.x + xOffset, this.y + this.height - 7, 7, 256 - 7, thisWidth, 7);
			xOffset += thisWidth;
		}
		this.blit(poseStack, this.x + this.width - 7, this.y + this.height - 7, 256 - 7, 256 - 7, 7, 7);
		
		//Render the text
		List<MutableComponent> logText = this.getLogText();
		int yPos = 0;
		for(int i = logText.size() - 1 - scroll; i >= 0 && yPos < this.height - 20; i--)
		{
			int thisHeight = this.font.wordWrapHeight(logText.get(i).getString(), this.width - 20);
			if(yPos + thisHeight <= this.height - 20)
				this.font.drawWordWrap(logText.get(i), this.x + 10, this.y + 10 + yPos, this.width - 20, 0xFFFFFF);
			yPos += thisHeight;
		}
		
	}
	
	private List<MutableComponent> getLogText() {
		TextLogger logger = this.logger.get();
		if(logger != null)
			return logger.logText;
		return new ArrayList<>();
	}
	
	private boolean canScrollDown()
	{
		//Faux render the text, to see if we overflow the height limit or not
		List<MutableComponent> logText = this.logger.get().logText;
		int yPos = 0;
		for(int i = logText.size() - 1 - scroll; i >= 0 && yPos < this.height - 20; i--)
		{
			yPos += this.font.wordWrapHeight(logText.get(i).getString(), this.width - 20);
		}
		return yPos >= this.height - 20;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta)
	{
		if(!this.visible)
			return false;
		
		if(delta < 0)
		{			
			if(this.canScrollDown())
				scroll++;
			else
				return false;
		}
		else if(delta > 0)
		{
			if(scroll > 0)
				scroll--;
			else
				return false;
		}
		
		return true;
	}

	@Override
	public void updateNarration(NarrationElementOutput narration) { }
	
	
}
