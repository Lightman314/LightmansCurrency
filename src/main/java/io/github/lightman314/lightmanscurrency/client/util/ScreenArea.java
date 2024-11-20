package io.github.lightman314.lightmanscurrency.client.util;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@Immutable
public final class ScreenArea {

    public final int x;
    public final int y;
    public final ScreenPosition pos;
    public final int width;
    public final int height;

    private ScreenArea(ScreenPosition pos, int width, int height) {
        this.x = pos.x;
        this.y = pos.y;
        this.pos = pos;
        this.width = width;
        this.height = height;
    }

    public static ScreenArea of(int x, int y, int width, int height) { return of(ScreenPosition.of(x,y),width,height); }
    public static ScreenArea of(ScreenPosition position, int width, int height) { return new ScreenArea(position, width, height); }

    public boolean isMouseInArea(ScreenPosition mousePos) { return this.isMouseInArea(mousePos.x, mousePos.y); }
    public boolean isMouseInArea(int mouseX, int mouseY) { return mouseX >= this.pos.x && mouseX < this.pos.x + this.width && mouseY >= this.pos.y && mouseY < this.pos.y + this.height; }
    public boolean isMouseInArea(double mouseX, double mouseY) { return mouseX >= this.pos.x && mouseX < this.pos.x + this.width && mouseY >= this.pos.y && mouseY < this.pos.y + this.height; }

    public ScreenArea atPosition(int x, int y) { return of(x, y, this.width, this.height); }
    public ScreenArea atPosition(ScreenPosition newPos) { return of(newPos, this.width, this.height); }
    public ScreenArea offsetPosition(int x, int y) { return of(this.pos.offset(x,y), this.width, this.height); }
    public ScreenArea offsetPosition(ScreenPosition offset) { return of(this.pos.offset(offset), this.width, this.height); }
    public ScreenArea ofSize(int width, int height) { return of(this.pos, width, height); }
    public ScreenArea shrinkWidth(int widthDelta) { return of(this.pos,this.width - widthDelta,this.height); }
    public ScreenArea shrinkHeight(int heightDelta) { return of(this.pos,this.width,this.height - heightDelta); }

    public boolean isOutside(ScreenArea area) { return !this.isMouseInArea(area.pos) || !isMouseInArea(area.pos.offset(area.width,area.height)); }

    @Override
    public String toString() {
        return this.x + "," + this.y + "[" + this.width + "," + this.height + "]";
    }

}
