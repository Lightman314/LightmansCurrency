package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public interface ITooltipWidget extends ITooltipSource {
    default List<ITextComponent> getTooltip(int mouseX, int mouseY) {
        if(this instanceof Widget)
        {
            Widget widget = (Widget)this;
            if(widget.isMouseOver(mouseX, mouseY))
                return this.getTooltip();
        }
        return null;
    }
}
