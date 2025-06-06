package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemAndBackgroundEntry extends DisplayEntry
{
    private final ItemStack item;
    private final Consumer<List<Component>> tooltipEditor;
    private final Pair<ResourceLocation,ResourceLocation> background;
    private final ScreenPosition backgroundOffset;

    private ItemAndBackgroundEntry(ItemStack item, @Nullable List<Component> forcedTooltip, @Nullable Consumer<List<Component>> tooltipEditor, Pair<ResourceLocation,ResourceLocation> background, ScreenPosition backgroundOffset) { super(forcedTooltip); this.item = item.copy(); this.background = background; this.backgroundOffset = backgroundOffset; this.tooltipEditor = tooltipEditor; }

    public static ItemAndBackgroundEntry of(ItemStack item, List<Component> tooltip, Pair<ResourceLocation,ResourceLocation> background) { return new ItemAndBackgroundEntry(item, tooltip, null, background, ScreenPosition.ZERO); }
    public static ItemAndBackgroundEntry of(ItemStack item, Consumer<List<Component>> tooltipEditor, Pair<ResourceLocation,ResourceLocation> background) { return new ItemAndBackgroundEntry(item, null, tooltipEditor, background, ScreenPosition.ZERO); }
    public static ItemAndBackgroundEntry of(ItemStack item, List<Component> tooltip, Pair<ResourceLocation,ResourceLocation> background, ScreenPosition backgroundOffset) { return new ItemAndBackgroundEntry(item, tooltip, null, background, backgroundOffset); }
    public static ItemAndBackgroundEntry of(ItemStack item, Consumer<List<Component>> tooltipEditor, Pair<ResourceLocation,ResourceLocation> background, ScreenPosition backgroundOffset) { return new ItemAndBackgroundEntry(item, null, tooltipEditor, background, backgroundOffset); }

    private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y, DisplayData area) {
        if(this.item.isEmpty())
            return;
        gui.resetColor();
        //Center the x & y positions
        int left = getTopLeft(x + area.xOffset(), area.width());
        int top = getTopLeft(y + area.yOffset(), area.height());
        gui.renderSlotBackground(this.background, this.backgroundOffset.offset(left, top));
        gui.renderItem(this.item, left, top);
    }

    @Override
    public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
        int left = getTopLeft(x + area.xOffset(), area.width());
        int top = getTopLeft(y + area.yOffset(), area.height());
        return mouseX >= left && mouseX < left + 16 && mouseY >= top && mouseY < top + 16;
    }

    @Override
    public boolean trySelfRenderTooltip(@Nonnull EasyGuiGraphics gui) {
        if(this.tooltipEditor == null)
        {
            List<Component> tooltip = this.getTooltip();
            if(!tooltip.isEmpty())
            {
                EasyScreenHelper.RenderItemTooltip(gui, this.item, tooltip);
                return true;
            }
            return false;
        }
        EasyScreenHelper.RenderItemTooltipWithModifiers(gui, this.item, this.tooltipEditor);
        return true;
    }

}