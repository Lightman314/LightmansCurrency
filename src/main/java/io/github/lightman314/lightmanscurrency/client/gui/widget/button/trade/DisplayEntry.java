package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display.*;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class DisplayEntry {

    private final List<Component> tooltip;

    @Deprecated
    protected DisplayEntry() { this.tooltip = null; }

    protected DisplayEntry(List<Component> tooltip) { this.tooltip = tooltip; }

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

    //ItemEntry
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(ItemStack item, int count) { return ItemEntry.of(item.copyWithCount(count)); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(ItemStack item, int count, Consumer<List<Component>> tooltipEditor) { return ItemEntry.of(item.copyWithCount(count), tooltipEditor); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(ItemStack item, int count, List<Component> tooltip) { return ItemEntry.of(item.copyWithCount(count),tooltip); }

    //ItemAndBackgroundEntry
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(ItemStack item, int count, List<Component> tooltip, Pair<ResourceLocation,ResourceLocation> background) { return ItemAndBackgroundEntry.of(item.copyWithCount(count), tooltip, background, ScreenPosition.ZERO); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(ItemStack item, int count, Consumer<List<Component>> tooltipEditor, Pair<ResourceLocation,ResourceLocation> background) { return ItemAndBackgroundEntry.of(item.copyWithCount(count), tooltipEditor, background, ScreenPosition.ZERO); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(ItemStack item, int count, List<Component> tooltip, Pair<ResourceLocation,ResourceLocation> background, ScreenPosition backgroundOffset) { return ItemAndBackgroundEntry.of(item.copyWithCount(count), tooltip, background, backgroundOffset); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(ItemStack item, int count, Consumer<List<Component>> tooltipEditor, Pair<ResourceLocation,ResourceLocation> background, ScreenPosition backgroundOffset) { return ItemAndBackgroundEntry.of(item.copyWithCount(count), tooltipEditor, background, backgroundOffset); }

    //EmptySlotEntry
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(Pair<ResourceLocation,ResourceLocation> background) { return EmptySlotEntry.of(background); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(Pair<ResourceLocation,ResourceLocation> background, List<Component> tooltip) { return EmptySlotEntry.of(background, tooltip); }

    //TextDisplayEntry
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(Component text, TextRenderUtil.TextFormatting format) { return TextDisplayEntry.of(text, format); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(Component text, TextRenderUtil.TextFormatting format, boolean fullHitbox) { return TextDisplayEntry.of(text, format, fullHitbox); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(Component text, TextRenderUtil.TextFormatting format, List<Component> tooltip) { return TextDisplayEntry.of(text, format, tooltip); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(Component text, TextRenderUtil.TextFormatting format, List<Component> tooltip, boolean fullHitbox) { return TextDisplayEntry.of(text, format, tooltip, fullHitbox); }

    //ScrollingTextEntry
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(Component text, int color) { return ScrollingTextEntry.of(text, color); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(Component text, int color, int width) { return ScrollingTextEntry.of(text, color, width); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(Component text, int color, List<Component> tooltip) { return ScrollingTextEntry.of(text, color, tooltip); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(Component text, int color, int width, List<Component> tooltip) { return ScrollingTextEntry.of(text, color, width, tooltip); }

    //Money Values
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(MoneyValue price) { return of(price, null, false); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(MoneyValue price, List<Component> additionalTooltips) { return of(price, additionalTooltips, false); }
    @Deprecated(since = "2.2.5.4")
    public static DisplayEntry of(MoneyValue price, List<Component> additionalTooltips, boolean tooltipOverride) { return price.getDisplayEntry(additionalTooltips, tooltipOverride); }

    public static DisplayEntry forMoney(MoneyValue price) { return price.getDisplayEntry(null,false); }
    public static DisplayEntry forMoneyWithAddedTooltip(MoneyValue price, List<Component> additionalTooltips) { return price.getDisplayEntry(additionalTooltips,false); }
    public static DisplayEntry forMoneyWithSetTooltip(MoneyValue price, List<Component> tooltip) { return price.getDisplayEntry(tooltip,true); }

}