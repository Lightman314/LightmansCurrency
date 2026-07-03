package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public interface TooltipSource {

    TooltipSource EMPTY = (w,m) -> null;

    List<Component> collectToolips(FancyWidget widget, ScreenPosition mouse);

    static TooltipSource simple(Component tooltip) { return simple(ImmutableList.of(tooltip)); }
    static TooltipSource simple(List<Component> tooltip) { return simple(tooltip,false); }
    static TooltipSource simple(Component tooltip,boolean showInactive) { return simple(ImmutableList.of(tooltip)); }
    static TooltipSource simple(List<Component> tooltip,boolean showInactive) {
        return (w,m) -> {
            if(showInactive || w.isActive())
                return tooltip;
            return null;
        };
    }

    static TooltipSource deferredSingle(Supplier<Component> tooltip) { return deferredSingle(tooltip,false); }
    static TooltipSource deferredList(Supplier<List<Component>> tooltip) { return deferredList(tooltip,false); }
    static TooltipSource deferredSingle(Supplier<Component> tooltip,boolean showInactive) { return deferredList(() -> ImmutableList.of(tooltip.get()),showInactive); }
    static TooltipSource deferredList(Supplier<List<Component>> tooltip,boolean showInactive) {
        return (w,m) -> {
            if(showInactive || w.isActive())
                return tooltip.get();
            return null;
        };
    }

}