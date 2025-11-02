package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab;

import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public interface ITab {
    @Nonnull
    IconData getIcon();
    @Nullable
    default Function<WidgetRotation,FixedSizeSprite> getSprite() { return null; }
    @Nullable
    Component getTooltip();
}
