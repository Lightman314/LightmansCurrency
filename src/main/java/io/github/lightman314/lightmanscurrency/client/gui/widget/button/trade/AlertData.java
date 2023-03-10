package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import java.util.function.UnaryOperator;

import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AlertData {

	public enum AlertType {
		HELPFUL(0x00FF00, TextFormatting.GREEN, 1),
		WARN(0xFF7F00, TextFormatting.GOLD, 3),
		ERROR(0xFF0000, TextFormatting.RED, 5);
		
		private final int priority;
		private final int color;
		private final UnaryOperator<Style> format;
		AlertType(int color, TextFormatting format, int priority) { this.color = color; this.format = s -> s.applyFormat(format); this.priority = priority;}
	}
	
	private final IFormattableTextComponent message;
	public final int priority;
	public final int color;
	private final UnaryOperator<Style> formatting;
	
	private AlertData(IFormattableTextComponent message, int priority, int color, UnaryOperator<Style> format) {
		this.message = message;
		this.priority = priority;
		this.color = color;
		this.formatting = format;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void setShaderColor(float mult) {
		float red = (float)(this.color >> 16 & 255) / 255.0f;
		float green = (float)(this.color >> 8 & 255) / 255.0f;
		float blue = (float)(this.color & 255) / 255.0f;
		RenderUtil.color4f(red * mult, green * mult, blue * mult, 1f);
	}
	
	public IFormattableTextComponent getFormattedMessage() {
		return this.message.withStyle(this.formatting);
	}
	
	public static int compare(AlertData a, AlertData b) {
		return Integer.compare(a.priority, b.priority) * -1;
	}
	
	/**
	 * Used to convert old alert messages into an error.
	 * Should only be used to convert the results of ITradeData.getAlerts(TradeContext)
	 */
	@Deprecated
	public static AlertData convert(ITextComponent oldAlert) {
		if(oldAlert instanceof IFormattableTextComponent)
			return error((IFormattableTextComponent)oldAlert);
		else
			return error(EasyText.literal(oldAlert.getString()));
	}
	
	public static AlertData helpful(IFormattableTextComponent message) { return of(message, AlertType.HELPFUL); }
	public static AlertData warn(IFormattableTextComponent message) { return of(message, AlertType.WARN); }
	public static AlertData error(IFormattableTextComponent message) { return of(message, AlertType.ERROR); }
	
	private static AlertData of(IFormattableTextComponent message, AlertType type) { return of(message, type.priority, type.color, type.format); }
	
	public static AlertData of(IFormattableTextComponent message, int priority, int color, UnaryOperator<Style> style) {
		return new AlertData(message, priority, color, style);
	}
	
}
