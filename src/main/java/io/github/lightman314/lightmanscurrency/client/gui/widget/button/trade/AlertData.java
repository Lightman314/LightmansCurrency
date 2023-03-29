package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import java.util.function.UnaryOperator;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AlertData {

	public enum AlertType {
		HELPFUL(0x00FF00, ChatFormatting.GREEN, 1),
		WARN(0xFF7F00, ChatFormatting.GOLD, 3),
		ERROR(0xFF0000, ChatFormatting.RED, 5);
		
		private final int priority;
		private final int color;
		private final UnaryOperator<Style> format;
		AlertType(int color, ChatFormatting format, int priority) { this.color = color; this.format = s -> s.applyFormat(format); this.priority = priority;}
	}
	
	private final MutableComponent message;
	public final int priority;
	public final int color;
	private final UnaryOperator<Style> formatting;
	
	private AlertData(MutableComponent message, int priority, int color, UnaryOperator<Style> format) {
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
		RenderSystem.setShaderColor(red * mult, green * mult, blue * mult, 1f);
	}
	
	public MutableComponent getFormattedMessage() {
		return this.message.withStyle(this.formatting);
	}
	
	public static final int compare(AlertData a, AlertData b) {
		return Integer.compare(a.priority, b.priority) * -1;
	}
	
	/*public static final List<AlertData> convert(List<Component> oldAlerts) {
		if(oldAlerts == null)
			return null;
		List<AlertData> result = new ArrayList<>();
		for(Component m : oldAlerts) result.add(convert(m));
		return result;
	}*/
	
	/**
	 * Used to convert old alert messages into an error.
	 * Should only be used to convert the results of ITradeData.getAlerts(TradeContext)
	 */
	@Deprecated
	public static AlertData convert(Component oldAlert) {
		if(oldAlert instanceof MutableComponent)
			return error((MutableComponent)oldAlert);
		else
			return error(Component.literal(oldAlert.getString()));
	}
	
	public static AlertData helpful(MutableComponent message) { return of(message, AlertType.HELPFUL); }
	public static AlertData warn(MutableComponent message) { return of(message, AlertType.WARN); }
	public static AlertData error(MutableComponent message) { return of(message, AlertType.ERROR); }
	
	private static AlertData of(MutableComponent message, AlertType type) { return of(message, type.priority, type.color, type.format); }
	
	public static AlertData of(MutableComponent message, int priority, int color, UnaryOperator<Style> style) {
		return new AlertData(message, priority, color, style);
	}
	
}
