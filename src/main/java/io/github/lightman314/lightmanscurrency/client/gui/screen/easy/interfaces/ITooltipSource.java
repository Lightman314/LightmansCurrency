package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public interface ITooltipSource {
    List<Component> getTooltip(int mouseX, int mouseY);
    List<Component> getTooltip();

    /**
     * Temporary function to allow use of ITooltipSource widgets
     * on screens that have not yet been converted to EasyScreens.
     */
    static List<Component> collectTooltips(List<?> objects, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        for(Object obj : objects)
        {
            if(obj instanceof ITooltipSource ts)
            {
                List<Component> objTooltip = ts.getTooltip(mouseX, mouseY);
                if(objTooltip != null)
                    tooltip.addAll(objTooltip);
            }
        }
        return tooltip;
    }

    /**
     * Temporary function to allow use of ITooltipSource widgets
     * on screens that have not yet been converted to EasyScreens.
     */
    static void renderTooltips(Screen screen, PoseStack pose, int mouseX, int mouseY) {
        List<Component> tooltips = collectTooltips(screen.children(), mouseX, mouseY);
        if(tooltips.size() > 0)
            screen.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
    }

}
