package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class TextLogWindow extends Widget{

	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/textlog.png");
	
	public static final int WIDTH = 256;
	public static final int HEIGHT = 256;
	
	//private static final int LINE_LIMIT = 10;
	private static final int TEXT_WIDTH = WIDTH - 20;
	private static final int TEXT_HEIGHT = HEIGHT - 30;
	//private static final int TEXT_SPACER = 3; //May be inaccurate
	
	private final Supplier<TextLogger> logger;
	private final FontRenderer font;
	
	int scroll = 0;
	
	//boolean debugMessage = true;
	//boolean canScrollDown = false;
	
	public TextLogWindow(int x, int y, Supplier<TextLogger> logger, FontRenderer font)
	{
		super(x,y, WIDTH, HEIGHT, new StringTextComponent(""));
		this.logger = logger;
		this.font = font;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
		//Render the background
		this.blit(matrixStack, this.x, this.y, 0, 0, WIDTH, HEIGHT);
		
		//Render the text (WILL need changing later)
		List<ITextComponent> logText = this.logger.get().logText;
		int yPos = 0;
		//int renderCount = 0;
		//int i;
		for(int i = logText.size() - 1 - scroll; i >= 0 && yPos < TEXT_HEIGHT; i--)
		{
			int thisHeight = this.font.getWordWrappedHeight(logText.get(i).getString(), TEXT_WIDTH);
			if(yPos + thisHeight <= TEXT_HEIGHT)
				this.font.func_238418_a_(logText.get(i), this.x + 10, this.y + 10 + yPos, TEXT_WIDTH, 0xFFFFFF);
				//this.font.func_243248_b(matrixStack, logText.get(i), this.x + 10, this.y + 10 + yPos, 0xFFFFFF);
			yPos += thisHeight;
			//renderCount++;
		}
		
		/* Old debug stuff trying to determine the best scroll stop determination method
		boolean test1 = yPos >= TEXT_HEIGHT; //Method 1
		boolean test2 = i >= 0; //Method 2
		boolean test3 = renderCount > 1; //Method 3
		boolean test4 = this.scroll < logText.size() - 1; //Method 4
		
		this.canScrollDown = test1;
		
		if(debugMessage)
		{
			LightmansCurrency.LogInfo("Scroll: " + scroll + " Method1: " + test1 + " Method2: " + test2 + " Method3: " + test3 + " Method4: " + test4);
			debugMessage = false;
		}*/
		
	}
	
	private boolean canScrollDown()
	{
		//Faux render the text, to see if we overflow the height limit or not
		List<ITextComponent> logText = this.logger.get().logText;
		int yPos = 0;
		for(int i = logText.size() - 1 - scroll; i >= 0 && yPos < TEXT_HEIGHT; i--)
		{
			yPos += this.font.getWordWrappedHeight(logText.get(i).getString(), TEXT_WIDTH);
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
		
		//debugMessage = true;
		
		return true;
	}
	
	
}
