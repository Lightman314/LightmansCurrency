package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ITooltipWidget extends ITooltipSource {
    default List<Component> getTooltip(int mouseX, int mouseY) {
        if(this instanceof AbstractWidget widget && widget.isMouseOver(mouseX, mouseY))
            return this.getTooltip();
        return null;
    }
}
