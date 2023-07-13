package io.github.lightman314.lightmanscurrency.client.util;

import java.util.List;
import java.util.function.UnaryOperator;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class TextRenderUtil {

	public static class TextFormatting
	{
		
		public enum Centering {
			TOP_LEFT(-1,1), TOP_CENTER(0,1), TOP_RIGHT(1,1),
			MIDDLE_LEFT(-1,0), MIDDLE_CENTER(0,0), MIDDLE_RIGHT(1,0),
			BOTTOM_LEFT(-1,-1), BOTTOM_CENTER(0,-1), BOTTOM_RIGHT(1,-1);
			
			private final int horiz;
			private final int vert;
			
			Centering(int horiz, int vert) { this.horiz = horiz; this.vert = vert; }
			public boolean isTop() { return vert > 0; }
			public boolean isMiddle() { return vert == 0; }
			public boolean isBottom() { return vert < 0; }
			public boolean isLeft() { return horiz < 0; }
			public boolean isCenter() { return horiz == 0; }
			public boolean isRight() { return horiz > 1; }
			
			public Centering makeTop() { return this.of(this.horiz, 1); }
			public Centering makeMiddle() { return this.of(this.horiz, 0); }
			public Centering makeBottom() { return this.of(this.horiz, -1); }
			
			public Centering makeLeft() { return this.of(-1, this.vert); }
			public Centering makeCenter() { return this.of(0, this.vert); }
			public Centering makeRight() { return this.of(1, this.vert); }
			
			private Centering of(int horiz, int vert) {
				for(Centering c : Centering.values())
				{
					if(c.horiz == horiz && c.vert == vert)
						return c;
				}
				return this;
			}
			
		}
		
		private Centering centering = Centering.MIDDLE_CENTER;
		public Centering centering() { return this.centering; }
		private int color = 0xFFFFFF;
		public int color() { return this.color; }
		
		private TextFormatting() {}
		
		public static TextFormatting create() { return new TextFormatting(); }
		
		public TextFormatting topEdge() { this.centering = this.centering.makeTop(); return this; }
		public TextFormatting middle() { this.centering = this.centering.makeMiddle(); return this; }
		public TextFormatting bottomEdge() { this.centering = this.centering.makeBottom(); return this; }
		
		public TextFormatting leftEdge() { this.centering = this.centering.makeLeft(); return this; }
		public TextFormatting centered() { this.centering = this.centering.makeCenter(); return this; }
		public TextFormatting rightEdge() { this.centering = this.centering.makeRight(); return this; }
		
		public TextFormatting color(int color) { this.color = color; return this; }
		
	}
	
	public static Font getFont() {
		Minecraft mc = Minecraft.getInstance();
		return mc.font;
	}
	
	public static Component fitString(String text, int width) { return fitString(text, width, "..."); }
	
	public static Component fitString(String text, int width, Style style) { return fitString(text, width, "...", style); }
	
	public static Component fitString(String text, int width, String edge) { return fitString(EasyText.literal(text), width, edge); }
	
	public static Component fitString(Component component, int width) { return fitString(component.getString(), width, "...", component.getStyle()); }
	
	public static Component fitString(Component component, int width, String edge) { return fitString(component.getString(), width, edge, component.getStyle()); }
	
	public static Component fitString(Component component, int width, Style style) { return fitString(component.getString(), width, "...", style); }
	
	public static Component fitString(Component component, int width, String edge, Style style) { return fitString(component.getString(), width, edge, style); }
	
	public static Component fitString(String text, int width, String edge, Style style) {
		Font font = getFont();
		if(font.width(EasyText.literal(text).withStyle(style)) <= width)
			return EasyText.literal(text).withStyle(style);
		while(font.width(EasyText.literal(text + edge).withStyle(style)) > width && text.length() > 0)
			text = text.substring(0, text.length() - 1);
		return EasyText.literal(text + edge).withStyle(style);
	}
	
	public static void drawCenteredText(EasyGuiGraphics gui, String string, int centerX, int yPos, int color) { drawCenteredText(gui, EasyText.literal(string), centerX, yPos, color); }
	public static void drawCenteredText(EasyGuiGraphics gui, Component component, int centerX, int yPos, int color) {
		Font font = getFont();
		int width = font.width(component);
		gui.drawString(component, centerX - (width/2), yPos, color);
	}
	
	public static void drawRightEdgeText(EasyGuiGraphics gui, String string, int rightPos, int yPos, int color) { drawRightEdgeText(gui, EasyText.literal(string), rightPos, yPos, color); }
	public static void drawRightEdgeText(EasyGuiGraphics gui, Component component, int rightPos, int yPos, int color) {
		Font font = getFont();
		int width = font.width(component);
		gui.drawString(component, rightPos - width, yPos, color);
	}
	
	public static void drawCenteredMultilineText(EasyGuiGraphics gui, String string, int leftPos, int width, int topPos, int color) { drawCenteredMultilineText(gui, EasyText.literal(string), leftPos, width, topPos, color); }
	public static void drawCenteredMultilineText(EasyGuiGraphics gui, Component component, int leftPos, int width, int topPos, int color) { 
		Font font = getFont();
		List<FormattedCharSequence> lines = font.split(component, width);
		float centerPos = (float)leftPos + ((float)width / 2f);
		for(int i = 0; i < lines.size(); ++i)
		{
			FormattedCharSequence line = lines.get(i);
			int lineWidth = font.width(line);
			gui.drawString(line, (int)(centerPos - ((float)lineWidth/2f)), (int)(topPos + font.lineHeight * i), color);
		}
	}
	
	public static void drawVerticallyCenteredMultilineText(EasyGuiGraphics gui, String string, int leftPos, int width, int topPos, int height, int color) { drawVerticallyCenteredMultilineText(gui, EasyText.literal(string), leftPos, width, topPos, height, color); }
	public static void drawVerticallyCenteredMultilineText(EasyGuiGraphics gui, Component component, int leftPos, int width, int topPos, int height, int color) {
		Font font = getFont();
		List<FormattedCharSequence> lines = font.split(component, width);
		float centerPos = (float)leftPos + ((float)width / 2f);
		float startHeight = (float)topPos + ((float)height / 2f) - ((float)(font.lineHeight * lines.size())/2f);
		for(int i = 0; i < lines.size(); ++i)
		{
			FormattedCharSequence line = lines.get(i);
			int lineWidth = font.width(line);
			gui.drawString(line, (int)(centerPos - ((float)lineWidth/2f)), (int)(startHeight + font.lineHeight * i), color);
		}
	}
	
	public static MutableComponent changeStyle(Component component, UnaryOperator<Style> styleChanges) {
		if(component instanceof MutableComponent mc) {
			return mc.withStyle(styleChanges);
		}
		return EasyText.empty().append(component).withStyle(component.getStyle()).withStyle(styleChanges);
	}
	
}
