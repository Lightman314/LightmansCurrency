package io.github.lightman314.lightmanscurrency.client.gui.util;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

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

    public static @NotNull Supplier<List<Component>> createToggleTooltip(@NotNull NonNullSupplier<Boolean> toggle, List<Component> trueTooltip, List<Component> falseTooltip) {
        return () -> {
            if(toggle.get())
                return trueTooltip;
            return falseTooltip;
        };
    }

    public static @NotNull Supplier<List<Component>> createToggleSingleTooltip(@NotNull NonNullSupplier<Boolean> toggle, Component trueTooltip, Component falseTooltip) {
        return () -> {
            if(toggle.get())
                return lazyList(trueTooltip);
            return lazyList(falseTooltip);
        };
    }

    public static @NotNull Supplier<List<Component>> createToggleTooltip(@NotNull NonNullSupplier<Boolean> toggle, Supplier<List<Component>> trueTooltip, Supplier<List<Component>> falseTooltip) {
        return () -> {
            if(toggle.get())
                return trueTooltip.get();
            return falseTooltip.get();
        };
    }

    public static @NotNull Supplier<List<Component>> createToggleSingleTooltip(@NotNull NonNullSupplier<Boolean> toggle, Supplier<Component> trueTooltip, Supplier<Component> falseTooltip) {
        return () -> {
            if(toggle.get())
                return lazyList(trueTooltip.get());
            return lazyList(falseTooltip.get());
        };
    }

}
