package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class DisplayEntry {

    private final List<Component> tooltip;

    @Deprecated
    protected DisplayEntry() { this.tooltip = null; }

    protected DisplayEntry (List<Component> tooltip) { this.tooltip = tooltip; }

    protected final Font getFont() {
        Minecraft m = Minecraft.getInstance();
        return m.font;
    }

    @Nonnull
    public List<Component> getTooltip() {
        if(this.tooltip == null)
            return new ArrayList<>();
        return this.tooltip;
    }

    public boolean trySelfRenderTooltip(@Nonnull EasyGuiGraphics gui) { return false; }

    public abstract void render(EasyGuiGraphics gui, int x, int y, DisplayData area);

    public abstract boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY);

    public static DisplayEntry of(ItemStack item, int count) { return new ItemEntry(item, count, null, null); }
    public static DisplayEntry of(ItemStack item, int count, Consumer<List<Component>> tooltipEditor) { return new ItemEntry(item, count, null, tooltipEditor); }
    public static DisplayEntry of(ItemStack item, int count, List<Component> tooltip) { return new ItemEntry(item, count, tooltip, null); }
    public static DisplayEntry of(ItemStack item, int count, List<Component> tooltip, Pair<ResourceLocation,ResourceLocation> background) { return new ItemAndBackgroundEntry(item, count, tooltip, null, background, ScreenPosition.ZERO); }
    public static DisplayEntry of(ItemStack item, int count, Consumer<List<Component>> tooltipEditor, Pair<ResourceLocation,ResourceLocation> background) { return new ItemAndBackgroundEntry(item, count, null, tooltipEditor, background, ScreenPosition.ZERO); }
    public static DisplayEntry of(ItemStack item, int count, List<Component> tooltip, Pair<ResourceLocation,ResourceLocation> background, ScreenPosition backgroundOffset) { return new ItemAndBackgroundEntry(item, count, tooltip, null, background, backgroundOffset); }
    public static DisplayEntry of(ItemStack item, int count, Consumer<List<Component>> tooltipEditor, Pair<ResourceLocation,ResourceLocation> background, ScreenPosition backgroundOffset) { return new ItemAndBackgroundEntry(item, count, null, tooltipEditor, background, backgroundOffset); }
    public static DisplayEntry of(Pair<ResourceLocation,ResourceLocation> background) { return new EmptySlotEntry(background, null); }
    public static DisplayEntry of(Pair<ResourceLocation,ResourceLocation> background, List<Component> tooltip) { return new EmptySlotEntry(background, tooltip); }
    public static DisplayEntry of(Component text, TextRenderUtil.TextFormatting format) { return new TextEntry(text, format, null, false); }
    public static DisplayEntry of(Component text, TextRenderUtil.TextFormatting format, boolean fullHitbox) { return new TextEntry(text, format, null, fullHitbox); }
    public static DisplayEntry of(Component text, TextRenderUtil.TextFormatting format, List<Component> tooltip) { return new TextEntry(text, format, tooltip, false); }
    public static DisplayEntry of(Component text, TextRenderUtil.TextFormatting format, List<Component> tooltip, boolean fullHitbox) { return new TextEntry(text, format, tooltip, fullHitbox); }
    public static DisplayEntry of(MoneyValue price) { return of(price, null, false); }
    public static DisplayEntry of(MoneyValue price, List<Component> additionalTooltips) { return of(price, additionalTooltips, false); }
    public static DisplayEntry of(MoneyValue price, List<Component> additionalTooltips, boolean tooltipOverride) { return price.getDisplayEntry(additionalTooltips, tooltipOverride); }

    private static class ItemEntry extends DisplayEntry
    {
        private final ItemStack item;
        private final Consumer<List<Component>> tooltipEditor;

        private ItemEntry(ItemStack item, int count, @Nullable List<Component> forcedTooltip, @Nullable Consumer<List<Component>> tooltipEditor) { super(forcedTooltip); this.item = item.copy(); this.item.setCount(count); this.tooltipEditor = tooltipEditor; }

        private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }

        @Override
        public void render(EasyGuiGraphics gui, int x, int y, DisplayData area) {
            if(this.item.isEmpty())
                return;
            gui.resetColor();
            //Center the x & y positions
            int left = getTopLeft(x + area.xOffset(), area.width());
            int top = getTopLeft(y + area.yOffset(), area.height());
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
            EasyScreenHelper.RenderItemTooltipWithModifiers(gui, this.item,this.tooltipEditor);
            return true;
        }
    }

    private static class EmptySlotEntry extends DisplayEntry
    {
        private final Pair<ResourceLocation,ResourceLocation> background;

        private EmptySlotEntry(Pair<ResourceLocation,ResourceLocation> background, List<Component> tooltip) { super(tooltip); this.background = background; }

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

    private static class ItemAndBackgroundEntry extends DisplayEntry
    {
        private final ItemStack item;
        private final Consumer<List<Component>> tooltipEditor;
        private final Pair<ResourceLocation,ResourceLocation> background;
        private final ScreenPosition backgroundOffset;

        private ItemAndBackgroundEntry(ItemStack item, int count, @Nullable List<Component> forcedTooltip, @Nullable Consumer<List<Component>> tooltipEditor, Pair<ResourceLocation,ResourceLocation> background, ScreenPosition backgroundOffset) { super(forcedTooltip); this.item = item.copy(); this.item.setCount(count); this.background = background; this.backgroundOffset = backgroundOffset; this.tooltipEditor = tooltipEditor; }

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

    private static class TextEntry extends DisplayEntry
    {

        private final Component text;
        private final TextRenderUtil.TextFormatting format;
        private final boolean fullHitbox;

        private TextEntry(Component text, TextRenderUtil.TextFormatting format, List<Component> tooltip, boolean fullHitbox) { super(tooltip); this.text = text; this.format = format; this.fullHitbox = fullHitbox; }

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

}
