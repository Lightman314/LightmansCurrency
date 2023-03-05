package io.github.lightman314.lightmanscurrency.client.util;

import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IEasyScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.common.util.LazyOptional;

public final class ScreenPosition {

    public static final ScreenPosition ZERO = of(0,0);

    public final int x;
    public final int y;
    private ScreenPosition(int x, int y) { this.x = x; this.y = y; }

    public ScreenPosition offset(ScreenPosition other) { return of(this.x + other.x, this.y + other.y); }
    public ScreenPosition offset(AbstractWidget widget) { return of(this.x + widget.x, this.y + widget.y); }
    public ScreenPosition offset(AbstractContainerScreen<?> screen) { return this.offset(getScreenCorner(screen)); }
    public ScreenPosition offset(IEasyScreen screen) { return this.offset(getScreenCorner(screen)); }

    public void setPosition(AbstractWidget widget) { widget.x = this.x; widget.y = this.y; }

    public boolean isMouseInArea(int mouseX, int mouseY, int width, int height) { return ScreenArea.of(this, width, height).isMouseInArea(mouseX, mouseY); }


    @Override
    public String toString() { return this.x + ", " + this.y; }

    public static ScreenPosition of(int x, int y) { return new ScreenPosition(x,y); }
    public static LazyOptional<ScreenPosition> ofOptional(int x, int y) { return LazyOptional.of(() -> of(x, y)); }
    public static ScreenPosition getScreenCorner(AbstractContainerScreen<?> screen) { return of(screen.getGuiLeft(), screen.getGuiTop()); }
    public static ScreenPosition getScreenCorner(IEasyScreen screen) { return of(screen.guiLeft(), screen.guiTop()); }

}
