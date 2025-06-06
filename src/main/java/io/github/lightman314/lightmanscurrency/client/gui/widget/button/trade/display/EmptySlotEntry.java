package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class EmptySlotEntry extends DisplayEntry
{
    private final Pair<ResourceLocation,ResourceLocation> background;

    private EmptySlotEntry(Pair<ResourceLocation,ResourceLocation> background, List<Component> tooltip) { super(tooltip); this.background = background; }

    public static EmptySlotEntry of(Pair<ResourceLocation,ResourceLocation> background) { return new EmptySlotEntry(background,null); }
    public static EmptySlotEntry of(Pair<ResourceLocation,ResourceLocation> background, List<Component> tooltip) { return new EmptySlotEntry(background,tooltip); }

    private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, DisplayData area) {
        gui.resetColor();
        int left = getTopLeft(x + area.xOffset(), area.width());
        int top = getTopLeft(y + area.yOffset(), area.height());
        gui.renderSlotBackground(this.background, left, top);
    }

    @Override
    public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
        int left = getTopLeft(x + area.xOffset(), area.width());
        int top = getTopLeft(y + area.yOffset(), area.height());
        return mouseX >= left && mouseX < left + 16 && mouseY >= top && mouseY < top + 16;
    }

}