package io.github.lightman314.lightmanscurrency.api.misc.client.sprites;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface FixedSizeSprite {

    int getWidth();
    int getHeight();

    default void render(EasyGuiGraphics gui, ScreenPosition position, AbstractWidget widget) { this.render(gui,position.x,position.y,widget); }
    default void render(EasyGuiGraphics gui, int x, int y, AbstractWidget widget) {
        if(this instanceof IWidgetContextSprite wcs)
            wcs.updateWidgetContext(widget);
        this.render(gui,x,y);
    }
    default void render(EasyGuiGraphics gui, ScreenPosition position) { this.render(gui,position.x,position.y); }
    void render(EasyGuiGraphics gui, int x, int y);

}
