package io.github.lightman314.lightmanscurrency.api.misc.client.sprites;

import net.minecraft.client.gui.components.AbstractWidget;

public interface IWidgetContextSprite {

    default void updateWidgetContext(AbstractWidget widget) { this.updateWidgetContext(widget.isActive(),widget.isHovered()); }
    void updateWidgetContext(boolean active,boolean hovered);
    FixedSizeSprite getSprite(boolean active,boolean focused);

}