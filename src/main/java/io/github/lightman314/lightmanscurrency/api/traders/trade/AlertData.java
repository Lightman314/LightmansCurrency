package io.github.lightman314.lightmanscurrency.api.traders.trade;

import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.network.chat.Component;

public class AlertData {

	private final Component message;
	public final AlertType type;

	private AlertData(Component message, AlertType type) {
		this.message = message;
		this.type = type;
	}

    public int getColor(boolean isHovered) { return isHovered ? this.type.hoverColor : this.type.color; }

	public Component getFormattedMessage() {
		return this.message.copy().withStyle(this.type.format);
	}
	
	public static int compare(AlertData a,AlertData b) { return Integer.compare(a.type.priority, b.type.priority) * -1; }
	
	public static AlertData helpful(Component message) { return of(message, AlertType.HELPFUL); }
	public static AlertData helpful(TextEntry message) { return of(message.get(), AlertType.HELPFUL); }
	public static AlertData neutral(Component message) { return of(message, AlertType.NEUTRAL); }
	public static AlertData neutral(TextEntry message) { return of(message.get(), AlertType.NEUTRAL); }
	public static AlertData warn(Component message) { return of(message, AlertType.WARN); }
	public static AlertData warn(TextEntry message) { return of(message.get(), AlertType.WARN); }
	public static AlertData error(Component message) { return of(message, AlertType.ERROR); }
	public static AlertData error(TextEntry message) { return of(message.get(), AlertType.ERROR); }

	public static AlertData of(Component message, AlertType type) { return new AlertData(message, type); }
	
}
