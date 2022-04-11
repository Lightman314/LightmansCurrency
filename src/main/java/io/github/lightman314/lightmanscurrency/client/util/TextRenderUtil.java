package io.github.lightman314.lightmanscurrency.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class TextRenderUtil {

	public static Font getFont() {
		Minecraft mc = Minecraft.getInstance();
		return mc.font;
	}
	
	public static Component fitString(String text, int width) { return fitString(text, width, "..."); }
	
	public static Component fitString(String text, int width, Style style) {
		return fitString(text, width, "...", style);
	}
	
	public static Component fitString(String text, int width, String edge) { return fitString(new TextComponent(text), width, edge); }
	
	public static Component fitString(Component component, int width) { return fitString(component.getString(), width, "...", component.getStyle()); }
	
	public static Component fitString(Component component, int width, String edge) { return fitString(component.getString(), width, edge, component.getStyle()); }
	
	public static Component fitString(Component component, int width, Style style) { return fitString(component.getString(), width, "...", style); }
	
	public static Component fitString(Component component, int width, String edge, Style style) { return fitString(component.getString(), width, edge, style); }
	
	public static Component fitString(String text, int width, String edge, Style style) {
		Font font = getFont();
		if(font.width(new TextComponent(text).withStyle(style)) < width)
			return new TextComponent(text).withStyle(style);
		while(font.width(new TextComponent(text + edge).withStyle(style)) > width && text.length() > 0)
			text = text.substring(0, text.length() - 1);
		return new TextComponent(text + edge).withStyle(style);
	}
	
}
