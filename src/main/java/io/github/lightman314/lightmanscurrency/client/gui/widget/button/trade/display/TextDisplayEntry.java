package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TextDisplayEntry extends DisplayEntry
{

    private final Component text;
    private final TextRenderUtil.TextFormatting format;
    private final boolean fullHitbox;

    private TextDisplayEntry(Component text, TextRenderUtil.TextFormatting format, List<Component> tooltip, boolean fullHitbox) { super(tooltip); this.text = text; this.format = format; this.fullHitbox = fullHitbox; }

    public static TextDisplayEntry of(Component text, TextRenderUtil.TextFormatting format) { return new TextDisplayEntry(text, format, null, false); }
    public static TextDisplayEntry of(Component text, TextRenderUtil.TextFormatting format, boolean fullHitbox) { return new TextDisplayEntry(text, format, null, fullHitbox); }
    public static TextDisplayEntry of(Component text, TextRenderUtil.TextFormatting format, List<Component> tooltip) { return new TextDisplayEntry(text, format, tooltip, false); }
    public static TextDisplayEntry of(Component text, TextRenderUtil.TextFormatting format, List<Component> tooltip, boolean fullHitbox) { return new TextDisplayEntry(text, format, tooltip, fullHitbox); }

    protected int getTextLeft(int x, int availableWidth) {
        if(this.format.centering().isCenter())
            return x + (availableWidth / 2) - (this.getTextWidth() / 2);
        if(this.format.centering().isRight())
            return x + availableWidth - this.getTextWidth();
        return x;
    }

    protected int getTextTop(int y, int availableHeight) {
        if(this.format.centering().isMiddle())
            return y + (availableHeight / 2) - (this.getFont().lineHeight / 2);
        if(this.format.centering().isBottom())
            return y + availableHeight - this.getFont().lineHeight;
        return y;
    }

    protected int getTextWidth() { return this.getFont().width(this.text); }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, DisplayData area) {
        if(this.text.getString().isBlank())
            return;
        gui.resetColor();
        //Define the x position
        int left = this.getTextLeft(x + area.xOffset(), area.width());
        //Define the y position
        int top = this.getTextTop(y + area.yOffset(), area.height());
        gui.resetColor();
        //Draw the text
        gui.drawShadowed(this.text, left, top, this.format.color());
    }

    @Override
    public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
        int left = this.fullHitbox ? x + area.xOffset() : this.getTextLeft(x + area.xOffset(), area.width());
        int top = this.fullHitbox ? y + area.yOffset() : this.getTextTop(y + area.yOffset(), area.height());
        int width = this.fullHitbox ? area.width() : this.getTextWidth();
        int height = this.fullHitbox ? area.height() : this.getFont().lineHeight;
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

}