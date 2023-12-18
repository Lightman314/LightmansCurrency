package io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ITooltipSource {
    List<Component> getTooltipText(int mouseX, int mouseY);

    default void renderTooltip(EasyGuiGraphics gui)
    {
        List<Component> tooltips = this.getTooltipText(gui.mousePos.x, gui.mousePos.y);
        if(tooltips != null && tooltips.size() > 0)
            gui.renderComponentTooltip(tooltips);
    }

}
