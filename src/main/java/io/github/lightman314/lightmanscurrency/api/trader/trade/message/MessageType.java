package io.github.lightman314.lightmanscurrency.api.trader.trade.message;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public record MessageType(int priority, int color, int hoverColor, float alpha, UnaryOperator<Style> format) {

    public static final MessageType HELPFUL = new MessageType(1,0x00FF00,0x008000,ChatFormatting.GREEN);
    public static final MessageType NEUTRAL = new MessageType(-100,0xFFFFFF,0x808080,UnaryOperator.identity());
    public static final MessageType WARN = new MessageType(3,0xFF7F00,0x804000,ChatFormatting.GOLD);
    public static final MessageType ERROR = new MessageType(5,0xFF0000,0x800000,ChatFormatting.RED);
    public static final MessageType INVISIBLE = new MessageType(Integer.MIN_VALUE,0xFFFFFF,0x008000,UnaryOperator.identity());

    public MessageType(int priority,int color,int hoverColor,ChatFormatting format) { this(priority,color,hoverColor,s -> s.applyFormat(format)); }
    public MessageType(int priority,int color,int hoverColor,UnaryOperator<Style> format) { this(priority,color,hoverColor,1f,format); }

}