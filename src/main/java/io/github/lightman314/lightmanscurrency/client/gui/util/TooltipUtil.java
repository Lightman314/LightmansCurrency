package io.github.lightman314.lightmanscurrency.client.gui.util;

import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TooltipUtil {

    @Nullable
    public static List<Component> lazyList(Component entry) {
        if(entry == null)
            return null;
        List<Component> list = new ArrayList<>();
        list.add(entry);
        return list;
    }

    @Nonnull
    public static Supplier<List<Component>> createToggleTooltip(@Nonnull Supplier<Boolean> toggle, List<Component> trueTooltip, List<Component> falseTooltip) {
        return () -> {
            if(toggle.get())
                return trueTooltip;
            return falseTooltip;
        };
    }

    @Nonnull
    public static Supplier<List<Component>> createToggleSingleTooltip(@Nonnull Supplier<Boolean> toggle, Component trueTooltip, Component falseTooltip) {
        return () -> {
            if(toggle.get())
                return lazyList(trueTooltip);
            return lazyList(falseTooltip);
        };
    }

    @Nonnull
    public static Supplier<List<Component>> createToggleTooltip(@Nonnull Supplier<Boolean> toggle, Supplier<List<Component>> trueTooltip, Supplier<List<Component>> falseTooltip) {
        return () -> {
            if(toggle.get())
                return trueTooltip.get();
            return falseTooltip.get();
        };
    }

    @Nonnull
    public static Supplier<List<Component>> createToggleSingleTooltip(@Nonnull Supplier<Boolean> toggle, Supplier<Component> trueTooltip, Supplier<Component> falseTooltip) {
        return () -> {
            if(toggle.get())
                return lazyList(trueTooltip.get());
            return lazyList(falseTooltip.get());
        };
    }

}
