package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class EasyAddonHelper {

    //Widget Active Modifiers
    public static WidgetAddon activeCheck(@Nonnull Function<EasyWidget,Boolean> shouldBeActive) { return new ActiveCheckAddon(shouldBeActive); }
    public static WidgetAddon activeCheck(@Nonnull Supplier<Boolean> shouldBeActive) { return new ActiveCheckAddon(b -> shouldBeActive.get()); }
    //Widget Visible Modifiers
    public static WidgetAddon visibleCheck(@Nonnull Function<EasyWidget,Boolean> shouldBeVisible) { return new VisibleCheckAddon(shouldBeVisible); }
    public static WidgetAddon visibleCheck(@Nonnull Supplier<Boolean> shouldBeVisible) { return new VisibleCheckAddon(b -> shouldBeVisible.get()); }

    //Widget Tooltip Modifiers
    public static WidgetAddon tooltip(@Nonnull Component tooltip) { return new TooltipAddon(Suppliers.memoize(() -> toList(tooltip))); }
    public static WidgetAddon tooltip(@Nonnull TextEntry tooltip) { return new TooltipAddon(Suppliers.memoize(() -> toList(tooltip.get()))); }
    public static WidgetAddon tooltip(@Nonnull MultiLineTextEntry tooltip) { return new TooltipAddon(Suppliers.memoize(tooltip::get)); }
    public static WidgetAddon tooltips(@Nonnull List<Component> tooltip) { return new TooltipAddon(Suppliers.memoize(() -> tooltip)); }
    public static WidgetAddon tooltip(@Nonnull Supplier<Component> tooltip) { return new TooltipAddon(() -> toList(tooltip.get())); }
    public static WidgetAddon tooltips(@Nonnull Supplier<List<Component>> tooltip) { return new TooltipAddon(tooltip); }

    public static WidgetAddon tooltips(@Nonnull Supplier<List<Component>> tooltip, int width) { return tooltips(tooltip,width,false); }
    public static WidgetAddon tooltips(@Nonnull Supplier<List<Component>> tooltip, int width, boolean ignoreActive) { return new TooltipSplitterAddon(tooltip,width,ignoreActive); }
    public static WidgetAddon tooltip(@Nonnull Component tooltip, int width) { return tooltip(tooltip,width,false); }
    public static WidgetAddon tooltip(@Nonnull Component tooltip, int width, boolean ignoreActive) { return new TooltipSplitterAddon(tooltip,width,ignoreActive); }
    public static WidgetAddon tooltip(@Nonnull TextEntry tooltip, int width) { return tooltip(tooltip,width,false); }
    public static WidgetAddon tooltip(@Nonnull TextEntry tooltip, int width, boolean ignoreActive) { return new TooltipSplitterAddon(tooltip.get(),width,ignoreActive); }

    //Fancier Tooltip Modifiers
    @Deprecated
    public static WidgetAddon additiveTooltip(@Nonnull String translationKey, @Nonnull Supplier<Object[]> inputSource) { return tooltip(() -> Component.translatable(translationKey, inputSource.get())); }
    @Deprecated
    public static WidgetAddon additiveTooltip2(@Nonnull String translationKey, @Nonnull Supplier<Object> inputSource) { return tooltip(() -> Component.translatable(translationKey, inputSource.get())); }
    public static WidgetAddon toggleTooltip(@Nonnull Supplier<Boolean> toggle, Component trueTooltip, Component falseTooltip) { return tooltip(() -> toggle.get() ? trueTooltip : falseTooltip); }
    public static WidgetAddon toggleTooltip(@Nonnull Supplier<Boolean> toggle, Supplier<Component> trueTooltip, Supplier<Component> falseTooltip) { return tooltip(() -> toggle.get() ? trueTooltip.get() : falseTooltip.get()); }
    public static WidgetAddon changingTooltip(@Nonnull Supplier<Integer> indicator, Component... tooltips)
    {
        if(tooltips.length == 0)
            return tooltip(EasyText.empty());
        return tooltip(() -> tooltips[MathUtil.clamp(indicator.get(), 0, tooltips.length - 1)]);
    }

    private static List<Component> toList(@Nullable Component tooltip)
    {
        if(tooltip == null)
            return ImmutableList.of();
        return ImmutableList.of(tooltip);
    }

    private static class ActiveCheckAddon extends WidgetAddon
    {
        private final Function<EasyWidget,Boolean> shouldBeActive;
        ActiveCheckAddon(Function<EasyWidget,Boolean> shouldBeActive) { this.shouldBeActive = shouldBeActive; }
        @Override
        public void activeTick() {
            EasyWidget widget = this.getWidget();
            if(widget != null)
                widget.setActive(this.shouldBeActive.apply(widget));
        }
    }

    private static class VisibleCheckAddon extends WidgetAddon
    {
        private final Function<EasyWidget,Boolean> shouldBeVisbile;
        VisibleCheckAddon(Function<EasyWidget,Boolean> shouldBeVisbile) { this.shouldBeVisbile = shouldBeVisbile; }
        @Override
        public void visibleTick() {
            EasyWidget widget = this.getWidget();
            if(widget != null)
                widget.setVisible(this.shouldBeVisbile.apply(widget));
        }
    }

    private static class TooltipAddon extends WidgetAddon implements ITooltipSource
    {

        private final Supplier<List<Component>> tooltip;
        TooltipAddon(@Nonnull Supplier<List<Component>> tooltip) { this.tooltip = tooltip; }

        @Override
        public List<Component> getTooltipText(int mouseX, int mouseY) {
            EasyWidget w = this.getWidget();
            if(w != null && w.isActive() && w.getArea().isMouseInArea(mouseX, mouseY))
                return this.tooltip.get();
            return null;
        }

    }

    private static class TooltipSplitterAddon extends WidgetAddon implements ITooltipSource
    {
        private final Supplier<List<Component>> tooltip;
        private final int width;
        private final boolean ignoreActive;
        TooltipSplitterAddon(@Nonnull Component tooltip, int width, boolean ignoreActive) { this(Lists.newArrayList(tooltip),width,ignoreActive); }
        TooltipSplitterAddon(@Nonnull List<Component> tooltip, int width, boolean ignoreActive) { this(() -> tooltip,width,ignoreActive); }
        TooltipSplitterAddon(@Nonnull Supplier<List<Component>> tooltip, int width, boolean ignoreActive) { this.tooltip = tooltip; this.width = width; this.ignoreActive = ignoreActive; }

        @Override
        public List<Component> getTooltipText(int mouseX, int mouseY) { return null; }

        @Override
        public void renderTooltip(EasyGuiGraphics gui) {
            EasyWidget w = this.getWidget();
            if(w != null && (this.ignoreActive || w.isActive()) && w.getArea().isMouseInArea(gui.mousePos))
            {
                List<FormattedCharSequence> t = new ArrayList<>();
                List<Component> lines = this.tooltip.get();
                if(lines.isEmpty())
                    return;
                for(Component c : lines)
                    t.addAll(gui.font.split(c,this.width));
                gui.renderTooltip(t);
            }

        }
    }

}
