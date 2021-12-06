package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class TextLogWindow extends AbstractWidget{

	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/textlog.png");
	
	public static final int WIDTH = 256;
	public static final int HEIGHT = 256;
	
	private static final int TEXT_WIDTH = WIDTH - 20;
	private static final int TEXT_HEIGHT = HEIGHT - 30;
	
	private final Supplier<TextLogger> logger;
	private final Font font;
	
	int scroll = 0;
	
	public TextLogWindow(int x, int y, Supplier<TextLogger> logger, Font font)
	{
		super(x,y, WIDTH, HEIGHT, new TextComponent(""));
		this.logger = logger;
		this.font = font;
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f,  1f,  1f, 1f);
		//Render the background
		this.blit(poseStack, this.x, this.y, 0, 0, WIDTH, HEIGHT);
		
		//Render the text (WILL need changing later)
		List<MutableComponent> logText = this.logger.get().logText;
		int yPos = 0;
		for(int i = logText.size() - 1 - scroll; i >= 0 && yPos < TEXT_HEIGHT; i--)
		{
			int thisHeight = this.font.wordWrapHeight(logText.get(i).getString(), TEXT_WIDTH);
			if(yPos + thisHeight <= TEXT_HEIGHT)
				this.font.drawWordWrap(logText.get(i), this.x + 10, this.y + 10 + yPos, TEXT_WIDTH, 0xFFFFFF);
			yPos += thisHeight;
		}
		
	}
	
	private boolean canScrollDown()
	{
		//Faux render the text, to see if we overflow the height limit or not
		List<MutableComponent> logText = this.logger.get().logText;
		int yPos = 0;
		for(int i = logText.size() - 1 - scroll; i >= 0 && yPos < TEXT_HEIGHT; i--)
		{
			yPos += this.font.wordWrapHeight(logText.get(i).getString(), TEXT_WIDTH);
		}
		return yPos >= TEXT_HEIGHT;
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
