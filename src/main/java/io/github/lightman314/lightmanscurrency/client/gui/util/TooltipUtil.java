package io.github.lightman314.lightmanscurrency.client.gui.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TooltipUtil {

    @Nullable
    public static List<ITextComponent> lazyList(ITextComponent entry) {
        if(entry == null)
            return null;
        List<ITextComponent> list = new ArrayList<>();
        list.add(entry);
        return list;
    }

    public static @Nonnull Supplier<List<ITextComponent>> createToggleTooltip(@Nonnull NonNullSupplier<Boolean> toggle, List<ITextComponent> trueTooltip, List<ITextComponent> falseTooltip) {
        return () -> {
            if(toggle.get())
                return trueTooltip;
            return falseTooltip;
        };
    }

    public static @Nonnull Supplier<List<ITextComponent>> createToggleSingleTooltip(@Nonnull NonNullSupplier<Boolean> toggle, ITextComponent trueTooltip, ITextComponent falseTooltip) {
        return () -> {
            if(toggle.get())
                return lazyList(trueTooltip);
            return lazyList(falseTooltip);
        };
    }

    public static @Nonnull Supplier<List<ITextComponent>> createToggleTooltip(@Nonnull NonNullSupplier<Boolean> toggle, Supplier<List<ITextComponent>> trueTooltip, Supplier<List<ITextComponent>> falseTooltip) {
        return () -> {
            if(toggle.get())
                return trueTooltip.get();
            return falseTooltip.get();
        };
    }

    public static @Nonnull Supplier<List<ITextComponent>> createToggleSingleTooltip(@Nonnull NonNullSupplier<Boolean> toggle, Supplier<ITextComponent> trueTooltip, Supplier<ITextComponent> falseTooltip) {
        return () -> {
            if(toggle.get())
                return lazyList(trueTooltip.get());
            return lazyList(falseTooltip.get());
        };
    }

}