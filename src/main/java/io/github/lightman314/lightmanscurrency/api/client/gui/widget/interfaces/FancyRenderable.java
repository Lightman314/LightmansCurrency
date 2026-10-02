package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.LayoutElement;

import javax.annotation.Nullable;

public interface FancyRenderable extends LayoutElement {

    default void setPosition(ScreenPosition position) { this.setPosition(position.x,position.y); }
    void setScissorArea(@Nullable ScreenArea area);
    default ScreenArea getArea() { return ScreenArea.of(this.getX(),this.getY(),this.getWidth(),this.getHeight()); }

    boolean isVisible();
    void setVisible(boolean visible);
    void alreadyRendered();

    void extractRenderState(GuiGraphicsExtractor gui,int mouseX,int mouseY,float a);

}