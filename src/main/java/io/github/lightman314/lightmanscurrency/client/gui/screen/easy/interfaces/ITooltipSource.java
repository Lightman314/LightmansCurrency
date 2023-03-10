package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public interface ITooltipSource {
    List<ITextComponent> getTooltip(int mouseX, int mouseY);
    List<ITextComponent> getTooltip();

    /**
     * Temporary function to allow use of ITooltipSource widgets
     * on screens that have not yet been converted to EasyScreens.
     */
    static List<ITextComponent> collectTooltips(List<?> objects, int mouseX, int mouseY) {
        List<ITextComponent> tooltip = new ArrayList<>();
        for(Object obj : objects)
        {
            if(obj instanceof ITooltipSource)
            {
                ITooltipSource ts = (ITooltipSource)obj;
                List<ITextComponent> objTooltip = ts.getTooltip(mouseX, mouseY);
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
    static void renderTooltips(Screen screen, MatrixStack pose, int mouseX, int mouseY) {
        List<ITextComponent> tooltips = collectTooltips(screen.children(), mouseX, mouseY);
        if(tooltips.size() > 0)
            screen.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
    }

}