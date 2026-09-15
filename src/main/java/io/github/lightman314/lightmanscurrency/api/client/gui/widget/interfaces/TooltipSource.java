package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public interface TooltipSource {

    TooltipSource EMPTY = (w,m) -> null;

    List<Component> collectToolips(FancyWidget widget,ScreenPosition mouse);

    default TooltipSource withAutoWrap(ChatFormatting... format) { return this.withAutoWrap(TooltipHelper.DEFAULT_TOOLTIP_WIDTH,format); }
    default TooltipSource withAutoWrap(int width,ChatFormatting... format) {
        return (w,m) -> TooltipHelper.splitTooltips(this.collectToolips(w,m),width,format);
    }

    static TooltipSource simple(TextEntry tooltip) { return simple(tooltip.get()); }
    static TooltipSource simple(Component tooltip) { return simple(List.of(tooltip)); }
    static TooltipSource simple(List<Component> tooltip) { return simple(tooltip,false); }
    static TooltipSource simple(Component tooltip,boolean showInactive) { return simple(List.of(tooltip)); }
    static TooltipSource simple(List<Component> tooltip,boolean showInactive) {
        return (w,m) -> {
            if(showInactive || w.isActive())
                return tooltip;
            return null;
        };
    }

    static TooltipSource simple(MultiLineTextEntry text) { return deferredList(text::get); }
    static TooltipSource deferredSingle(Supplier<Component> tooltip) { return deferredSingle(tooltip,false); }
    static TooltipSource deferredList(Supplier<List<Component>> tooltip) { return deferredList(tooltip,false); }
    static TooltipSource deferredSingle(Supplier<Component> tooltip,boolean showInactive) { return deferredList(() -> List.of(tooltip.get()),showInactive); }
    static TooltipSource deferredList(Supplier<List<Component>> tooltip,boolean showInactive) {
        return (w,m) -> {
            if(showInactive || w.isActive())
                return tooltip.get();
            return null;
        };
    }

}