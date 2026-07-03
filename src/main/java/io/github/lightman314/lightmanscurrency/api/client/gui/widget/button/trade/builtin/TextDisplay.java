package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.TradeDisplayEntry;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TextDisplay extends TradeDisplayEntry {

    private final Component text;
    private final List<Component> tooltips;
    private final int color;
    private final int width;
    private TextDisplay(Component text,List<Component> tooltips,int color,int width) {
        this.text = text;
        this.tooltips = tooltips;
        this.color = color;
        this.width = width;
    }

    public static TextDisplay of(Component text,int width) { return of(text,null,0x404040,width); }
    public static TextDisplay of(Component text,List<Component> tooltips,int width) { return of(text,tooltips,0x404040,width); }
    public static TextDisplay of(Component text,List<Component> tooltips,int color,int width) { return new TextDisplay(text,tooltips,color,width); }

    @Override
    public int desiredWidth() { return this.width; }

    @Override
    public void extractContents(FancyGuiExtractor gui, int x, int y) {
        Font font = gui.getFont();
        gui.scrollingText(this.text,x,y,this.width,18,this.color);
    }

    @Override
    public void appendTooltip(List<Component> tooltip) { tooltip.addAll(this.tooltips); }
}