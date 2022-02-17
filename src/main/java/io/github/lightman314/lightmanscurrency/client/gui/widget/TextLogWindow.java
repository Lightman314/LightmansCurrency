package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class TextLogWindow extends Widget{

	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/textlog.png");
	
	private final Supplier<TextLogger> logger;
	private final FontRenderer font;
	
	int scroll = 0;
	
	//OG
	public TextLogWindow(int x, int y, Supplier<TextLogger> logger, FontRenderer font)
	{
		super(x,y, 256, 256, new StringTextComponent(""));
		this.logger = logger;
		this.font = font;
	}
	
	@SuppressWarnings("resource")
	public TextLogWindow(ContainerScreen<?> screen, Supplier<TextLogger> logger)
	{
		super(screen.getGuiLeft(), screen.getGuiTop(), screen.getXSize(), screen.getYSize(), new StringTextComponent(""));
		this.logger = logger;
		this.font = screen.getMinecraft().fontRenderer;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		//Render the background
		//Render the top edge
		//Render the top edge
		this.blit(matrixStack, this.x, this.y, 0, 0, 7, 7);
		int xOffset = 7;
		while(xOffset < this.width - 7)
		{
			int thisWidth = Math.min(256 - 14, this.width - 7 - xOffset);
			this.blit(matrixStack, this.x + xOffset, this.y, 7, 0, thisWidth, 7);
			xOffset += thisWidth;
		}
		this.blit(matrixStack, this.x + this.width - 7, this.y, 256 - 7, 0, 7, 7);
		//Render the middle
		int yOffset = 7;
		while(yOffset < this.height - 7)
		{
			int thisHeight = Math.min(256 - 14, this.height - 7 - yOffset);
			this.blit(matrixStack, this.x, this.y + yOffset, 0, 7, 7, thisHeight);
			xOffset = 7;
			while(xOffset < this.width - 7)
			{
				int thisWidth = Math.min(256 - 14, this.width - 7 - xOffset);
				this.blit(matrixStack, this.x + xOffset, this.y + yOffset, 7, 7, thisWidth, thisHeight);
				xOffset += thisWidth;
			}
			this.blit(matrixStack, this.x + this.width - 7, this.y + yOffset, 256 - 7, 7, 7, thisHeight);
			yOffset += thisHeight;
		}
		//Render the bottom edge
		this.blit(matrixStack, this.x, this.y + this.height - 7, 0, 256 - 7, 7, 7);
		xOffset = 7;
		while(xOffset < this.width - 7)
		{
			int thisWidth = Math.min(256 - 14, this.width - 7 - xOffset);
			this.blit(matrixStack, this.x + xOffset, this.y + this.height - 7, 7, 256 - 7, thisWidth, 7);
			xOffset += thisWidth;
		}
		this.blit(matrixStack, this.x + this.width - 7, this.y + this.height - 7, 256 - 7, 256 - 7, 7, 7);
		
		//Render the text
		List<ITextComponent> logText = this.logger.get().logText;
		int yPos = 0;
		for(int i = logText.size() - 1 - scroll; i >= 0 && yPos < this.height - 20; i--)
		{
			int thisHeight = this.font.getWordWrappedHeight(logText.get(i).getString(), this.width - 20);
			if(yPos + thisHeight <= this.height - 20)
				this.font.func_238418_a_(logText.get(i), this.x + 10, this.y + 10 + yPos, this.width - 20, 0xFFFFFF);
			yPos += thisHeight;
		}
		
	}
	
	private boolean canScrollDown()
	{
		//Faux render the text, to see if we overflow the height limit or not
		List<ITextComponent> logText = this.logger.get().logText;
		int yPos = 0;
		for(int i = logText.size() - 1 - scroll; i >= 0 && yPos < this.height - 20; i--)
		{
			yPos += this.font.getWordWrappedHeight(logText.get(i).getString(), this.width - 20);
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
		
		//debugMessage = true;
		
		return true;
	}
	
	
}
