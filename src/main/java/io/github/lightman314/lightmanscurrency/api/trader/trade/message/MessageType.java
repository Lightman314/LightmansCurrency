package io.github.lightman314.lightmanscurrency.api.trader.trade.message;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public record MessageType(int priority,int color,int hoverColor,UnaryOperator<Style> format) implements UnaryOperator<Style> {

    public static final MessageType HELPFUL = new MessageType(1,0xFF00FF00,0xFF008000,ChatFormatting.GREEN);
    public static final MessageType NEUTRAL = new MessageType(-100,0xFFFFFFFF,0xFF808080,UnaryOperator.identity());
    public static final MessageType WARN = new MessageType(3,0xFFFF7F00,0xFF804000,ChatFormatting.GOLD);
    public static final MessageType ERROR = new MessageType(5,0xFFFF0000,0xFF800000,ChatFormatting.RED);
    public static final MessageType INVISIBLE = new MessageType(Integer.MIN_VALUE,0,0,UnaryOperator.identity());

    public MessageType(int priority,int color,int hoverColor,ChatFormatting format) { this(priority,color,hoverColor,s -> s.applyFormat(format)); }

    @Override
    public Style apply(Style style) { return this.format.apply(style); }

}