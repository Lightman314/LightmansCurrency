package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import javax.annotation.Nonnull;
import java.util.function.UnaryOperator;

public class AlertType {

    public static final AlertType HELPFUL = new AlertType(1, 0x00FF00, 0x008000, ChatFormatting.GREEN);
    public static final AlertType NEUTRAL = new AlertType(-100, 0xFFFFFF, 0x808080, s->s);
    public static final AlertType WARN = new AlertType(3, 0xFF7F00, 0x804000, ChatFormatting.GOLD);
    public static final AlertType ERROR = new AlertType(5, 0xFF0000, 0x800000, ChatFormatting.RED);

    public final int priority;
    public final int color;
    public final int hoverColor;
    public final UnaryOperator<Style> format;

    public AlertType(int priority, int color, int hoverColor, @Nonnull ChatFormatting format) { this(priority, color, hoverColor, s -> s.applyFormat(format)); }
    public AlertType(int priority, int color, int hoverColor, @Nonnull UnaryOperator<Style> format)
    {
        this.priority = priority;
        this.color = color;
        this.hoverColor = hoverColor;
        this.format = format;
    }

}
