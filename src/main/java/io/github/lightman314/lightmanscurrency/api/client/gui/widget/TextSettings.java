package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

public record TextSettings(int textColor, int fancyTextColor) {

    public static final int WHITE = 0xFFFFFFFF;
    public static final int GRAY = 0xFF404040;

    public static final TextSettings DEFAULT = new TextSettings(GRAY,WHITE);

    public TextSettings ofTextColor(int textColor) { return new TextSettings(textColor,this.fancyTextColor); }
    public TextSettings ofFancyTextColor(int fancyTextColor) { return new TextSettings(this.textColor,fancyTextColor); }

}
