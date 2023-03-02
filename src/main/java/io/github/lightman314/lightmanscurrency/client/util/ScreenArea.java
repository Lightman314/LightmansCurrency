package io.github.lightman314.lightmanscurrency.client.util;

public final class ScreenArea {

    public final int x;
    public final int y;
    public final ScreenPosition pos;
    public final int width;
    public final int height;

    private ScreenArea(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.pos = ScreenPosition.of(x,y);
        this.width = width;
        this.height = height;
    }

    public static ScreenArea of(int x, int y, int width, int height) { return new ScreenArea(x,y, width, height); }
    public static ScreenArea of(ScreenPosition position, int width, int height) { return new ScreenArea(position.x, position.y, width, height); }

    public boolean isMouseInArea(int mouseX, int mouseY) { return mouseX >= this.pos.x && mouseX < this.pos.x + this.width && mouseY >= this.pos.y && mouseY < this.pos.y + this.height; }

    public ScreenArea atPosition(ScreenPosition newPos) { return of(newPos, this.width, this.height); }

}