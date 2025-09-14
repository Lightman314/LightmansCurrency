package io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public interface ITooltipSource {

    @Nullable
    List<Component> getTooltipText(int mouseX, int mouseY);

    default boolean renderTooltip(EasyGuiGraphics gui)
    {
        List<Component> tooltips = this.getTooltipText(gui.mousePos.x, gui.mousePos.y);
        if(tooltips != null && !tooltips.isEmpty())
        {
            gui.renderComponentTooltip(tooltips);
            return true;
        }
        return false;
    }

}
