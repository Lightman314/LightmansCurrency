package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab;

import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITab {
    @Nonnull
    IconData getIcon();
    default int getColor() { return 0xFFFFFF; }
    @Nullable
    Component getTooltip();
}
