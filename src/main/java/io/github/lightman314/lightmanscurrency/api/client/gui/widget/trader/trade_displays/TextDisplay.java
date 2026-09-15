package io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeTooltipBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TextDisplay extends TradeDisplayEntry {

    private final Component text;
    private final List<Component> tooltips;
    private final int color;
    private final int width;
    private TextDisplay(Component text,List<Component> tooltips, int color, int width) {
        this.text = text;
        this.tooltips = tooltips;
        this.color = color;
        this.width = width;
    }

    public static TextDisplay of(Component text,int width) { return of(text,List.of(),0xFFFFFFFF,width); }
    public static TextDisplay of(Component text,List<Component> tooltips,int width) { return of(text,tooltips,0xFFFFFFFF,width); }
    public static TextDisplay of(Component text,List<Component> tooltips,int color,int width) { return new TextDisplay(text,tooltips,color,width); }

    @Override
    public int desiredWidth() { return this.width; }

    @Override
    public void extractContents(FancyGuiExtractor gui, int x, int y) {
        Font font = gui.getFont();
        gui.scrollingText(this.text,x,y,this.width,TradeButton.HEIGHT,this.color,false);
    }

    @Override
    public void appendTooltip(TradeTooltipBuilder builder) { builder.add(this.tooltips); }
}