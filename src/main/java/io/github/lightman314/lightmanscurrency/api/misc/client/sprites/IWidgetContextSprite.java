package io.github.lightman314.lightmanscurrency.api.misc.client.sprites;

import net.minecraft.client.gui.components.AbstractWidget;

public interface IWidgetContextSprite {

    void updateWidgetContext(AbstractWidget widget);
    FixedSizeSprite getSprite(boolean active,boolean focused);

}