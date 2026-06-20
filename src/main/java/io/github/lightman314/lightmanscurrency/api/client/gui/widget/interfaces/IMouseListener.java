package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import net.minecraft.client.input.MouseButtonEvent;

public interface IMouseListener {

    boolean onMouseClicked(MouseButtonEvent event,boolean doubleClick);
    default boolean onMouseReleased(MouseButtonEvent event) { return false; }

}
