package io.github.lightman314.lightmanscurrency.client.util;

import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.common.util.LazyOptional;

public final class ScreenPosition {

    public static final ScreenPosition ZERO = of(0,0);

    public final int x;
    public final int y;
    private ScreenPosition(int x, int y) { this.x = x; this.y = y; }

    public ScreenPosition offset(ScreenPosition other) { return other != null ? of(this.x + other.x, this.y + other.y) : this; }
    public ScreenPosition offset(int x, int y) { return of(this.x + x, this.y + y); }
    public ScreenPosition offset(AbstractWidget widget) { return widget != null ? of(this.x + widget.getX(), this.y + widget.getY()) : this; }
    public ScreenPosition offset(IEasyScreen screen) { return screen != null ? this.offset(screen.getCorner()) : this; }
    public ScreenPosition offsetScreen(AbstractContainerScreen<?> screen) { return screen != null ? this.offset(getScreenCorner(screen)) : this; }

    public void setPosition(AbstractWidget widget) { widget.setPosition(this.x, this.y); }

    public boolean isMouseInArea(ScreenPosition mousePos, int width, int height) { return ScreenArea.of(this, width, height).isMouseInArea(mousePos); }
    public boolean isMouseInArea(int mouseX, int mouseY, int width, int height) { return ScreenArea.of(this, width, height).isMouseInArea(mouseX, mouseY); }
    public boolean isMouseInArea(double mouseX, double mouseY, int width, int height) { return ScreenArea.of(this, width, height).isMouseInArea(mouseX, mouseY); }

    public ScreenArea asArea(int width, int height) { return ScreenArea.of(this, width, height); }


    @Override
    public String toString() { return this.x + ", " + this.y; }

    public static ScreenPosition of(int x, int y) { return new ScreenPosition(x,y); }
    public static ScreenPosition of(double x, double y) { return of((int)x,(int)y); }
    public static ScreenPosition of(ScreenPosition offset, int x, int y) { return offset != null ? offset.offset(x,y) : of(x,y); }
    public static ScreenPosition of(IEasyScreen screen, int x, int y) { return screen != null ? screen.getCorner().offset(x,y) : of(x,y); }
    public static LazyOptional<ScreenPosition> ofOptional(int x, int y) { return LazyOptional.of(() -> of(x, y)); }
    public static ScreenPosition getScreenCorner(AbstractContainerScreen<?> screen) { return screen != null ? of(screen.getGuiLeft(), screen.getGuiTop()) : ScreenPosition.ZERO; }

}
