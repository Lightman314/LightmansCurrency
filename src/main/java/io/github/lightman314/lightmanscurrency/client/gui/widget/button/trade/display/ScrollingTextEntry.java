package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ScrollingTextEntry extends DisplayEntry
{

    private final Component text;
    private final int color;
    private final int width;
    private ScrollingTextEntry(Component text, int color, int width, List<Component> tooltip)
    {
        super(tooltip);
        this.text = text;
        this.color = color;
        this.width = width;
    }

    public static ScrollingTextEntry of(Component text, int color) { return new ScrollingTextEntry(text, color, 0,null); }
    public static ScrollingTextEntry of(Component text, int color, int width) { return new ScrollingTextEntry(text, color, width,null); }
    public static ScrollingTextEntry of(Component text, int color, List<Component> tooltip) { return new ScrollingTextEntry(text, color, 0, tooltip); }
    public static ScrollingTextEntry of(Component text, int color, int width, List<Component> tooltip) { return new ScrollingTextEntry(text, color, width, tooltip); }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, DisplayData area) {
        int left = x + area.xOffset();
        int top = y + area.yOffset();
        gui.drawScrollingString(this.text,left,top,area.width(),area.height(),this.color);
    }

    @Override
    public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
        int left = x + area.xOffset();
        int top = y + area.yOffset();
        return mouseX >= left && mouseX < left + area.width() && mouseY >= top && mouseY < top + area.height();
    }

    @Nonnull
    @Override
    public List<Component> getTooltip() {
        if(this.width > 0 && this.getFont().width(this.text) > this.width)
        {
            List<Component> tooltips = new ArrayList<>();
            tooltips.addAll(TooltipHelper.splitTooltips(this.text,TooltipHelper.DEFAULT_TOOLTIP_WIDTH));
            tooltips.addAll(super.getTooltip());
            return tooltips;
        }
        return super.getTooltip();
    }
}