package io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.builtin;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.TradeDisplayEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public class SpriteDisplay extends TradeDisplayEntry {

    private final Identifier sprite;
    private final int width;
    private final int height;
    private final int color;
    private final List<Component> tooltips;
    private SpriteDisplay(Identifier sprite,int width,int height,int color,List<Component> tooltips) {
        this.sprite = sprite;
        this.width = width;
        this.height = height;
        this.color = color;
        this.tooltips = tooltips;
    }

    public static SpriteDisplay of(Identifier sprite,int width,int height) { return of(sprite,width,height,-1); }
    public static SpriteDisplay of(Identifier sprite,int width,int height,int color) { return of(sprite,width,height,color,ImmutableList.of()); }
    public static SpriteDisplay of(Identifier sprite,int width,int height,List<Component> tooltips) { return new SpriteDisplay(sprite,width,height,-1,tooltips); }
    public static SpriteDisplay of(Identifier sprite,int width,int height,int color,List<Component> tooltips) { return new SpriteDisplay(sprite,width,height,color,tooltips); }

    @Override
    public int desiredWidth() { return this.width; }

    @Override
    public void extractContents(FancyGuiExtractor gui, int x, int y) {
        gui.blitSprite(this.sprite,x,y,this.width,this.height,this.color);
    }

    @Override
    public void appendTooltip(List<Component> tooltip) {
        tooltip.addAll(this.tooltips);
    }
}