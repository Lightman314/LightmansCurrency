package io.github.lightman314.lightmanscurrency.client.util;

import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@Immutable
public final class ScreenPosition {

    public static final ScreenPosition ZERO = of(0,0);

    public final int x;
    public final int y;
    private ScreenPosition(int x, int y) { this.x = x; this.y = y; }

    public ScreenPosition offset(ScreenPosition other) { return of(this.x + other.x, this.y + other.y); }
    public ScreenPosition offset(int x, int y) { return of(this.x + x, this.y + y); }
    public ScreenPosition offset(AbstractWidget widget) { return of(this.x + widget.getX(), this.y + widget.getY()); }
    public ScreenPosition offset(IEasyScreen screen) { return this.offset(screen.getCorner()); }
    public ScreenPosition offsetScreen(AbstractContainerScreen<?> screen) { return this.offset(getScreenCorner(screen)); }

    public void setPosition(AbstractWidget widget) { widget.setPosition(this.x, this.y); }

    public boolean isMouseInArea(ScreenPosition mousePos, int width, int height) { return ScreenArea.of(this, width, height).isMouseInArea(mousePos); }
    public boolean isMouseInArea(int mouseX, int mouseY, int width, int height) { return ScreenArea.of(this, width, height).isMouseInArea(mouseX, mouseY); }
    public boolean isMouseInArea(double mouseX, double mouseY, int width, int height) { return ScreenArea.of(this, width, height).isMouseInArea(mouseX, mouseY); }

    public ScreenArea asArea(int width, int height) { return ScreenArea.of(this, width, height); }



    public static ScreenPosition of(int x, int y) { return new ScreenPosition(x,y); }
    public static ScreenPosition of(double x, double y) { return of((int)x,(int)y); }
    public static ScreenPosition of(ScreenPosition offset, int x, int y) { return offset.offset(x,y); }
    public static ScreenPosition of(IEasyScreen screen, int x, int y) { return screen.getCorner().offset(x,y); }
    public static Optional<ScreenPosition> ofOptional(int x, int y) { return Optional.of(of(x, y)); }
    public static ScreenPosition getScreenCorner(AbstractContainerScreen<?> screen) { return of(screen.getGuiLeft(), screen.getGuiTop()); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ScreenPosition pos)
            return pos.x == this.x && pos.y == this.y;
        return false;
    }
    @Override
    public int hashCode() { return Objects.hash(this.x,this.y); }
    @Override
    public String toString() { return this.x + "," + this.y; }

}
