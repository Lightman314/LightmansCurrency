package io.github.lightman314.lightmanscurrency.common.items.data.register;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public enum TransactionHelpfulness {
    NEUTRAL(0,s -> s),HELPFUL(0x00C000,ChatFormatting.GREEN),HARMFUL(0xC00000,ChatFormatting.RED);
    public final int textColor;
    public final UnaryOperator<Style> style;
    TransactionHelpfulness(int textColor,UnaryOperator<Style> style) { this.textColor = textColor; this.style = style; }
    TransactionHelpfulness(int textColor, ChatFormatting style) { this(textColor,s -> s.applyFormat(style)); }

}
