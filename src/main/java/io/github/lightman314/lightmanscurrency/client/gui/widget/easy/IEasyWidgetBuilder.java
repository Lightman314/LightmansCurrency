package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.NonNullSupplier;

import java.util.List;
import java.util.function.Supplier;

public interface IEasyWidgetBuilder {

    ScreenPosition getPosition();
    int getWidth();
    int getHeight();
    Supplier<List<Component>> getTooltip();

    boolean isFixedSize();

    NonNullSupplier<Boolean> getVisibilityCheck();
    NonNullSupplier<Boolean> getActiveCheck();

}
