package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class AlertData {

	private final MutableComponent message;
	public final AlertType type;

	private AlertData(@Nonnull MutableComponent message, @Nonnull AlertType type) {
		this.message = message;
		this.type = type;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void setShaderColor(@Nonnull EasyGuiGraphics gui, float mult, boolean isHovered) {
		int color = isHovered ? this.type.hoverColor : this.type.color;
		float red = (float)(color >> 16 & 255) / 255.0f;
		float green = (float)(color >> 8 & 255) / 255.0f;
		float blue = (float)(color & 255) / 255.0f;
		gui.setColor(red * mult, green * mult, blue * mult,this.type.alpha);
	}

	public MutableComponent getFormattedMessage() {
		return this.message.withStyle(this.type.format);
	}
	
	public static int compare(AlertData a, AlertData b) { return Integer.compare(a.type.priority, b.type.priority) * -1; }
	
	public static AlertData helpful(@Nonnull MutableComponent message) { return of(message, AlertType.HELPFUL); }
	public static AlertData helpful(@Nonnull TextEntry message) { return of(message.get(), AlertType.HELPFUL); }
	public static AlertData neutral(@Nonnull MutableComponent message) { return of(message, AlertType.NEUTRAL); }
	public static AlertData neutral(@Nonnull TextEntry message) { return of(message.get(), AlertType.NEUTRAL); }
	public static AlertData warn(@Nonnull MutableComponent message) { return of(message, AlertType.WARN); }
	public static AlertData warn(@Nonnull TextEntry message) { return of(message.get(), AlertType.WARN); }
	public static AlertData error(@Nonnull MutableComponent message) { return of(message, AlertType.ERROR); }
	public static AlertData error(@Nonnull TextEntry message) { return of(message.get(), AlertType.ERROR); }

	public static AlertData of(@Nonnull MutableComponent message, @Nonnull AlertType type) { return new AlertData(message, type); }
	
}
