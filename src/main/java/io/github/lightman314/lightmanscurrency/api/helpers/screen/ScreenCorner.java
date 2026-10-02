package io.github.lightman314.lightmanscurrency.api.helpers.screen;

import java.util.function.BiFunction;

public enum ScreenCorner {
    TOP_LEFT((w,h) -> ScreenPosition.ZERO),
    TOP_RIGHT((w,h) -> ScreenPosition.of(w,0)),
    BOTTOM_LEFT((w,h) -> ScreenPosition.of(0,h)),
    BOTTOM_RIGHT(ScreenPosition::of);

    private final BiFunction<Integer,Integer,ScreenPosition> corner;
    ScreenCorner(BiFunction<Integer,Integer,ScreenPosition> corner) { this.corner = corner; }

    public boolean isLeft() { return this == TOP_LEFT || this == BOTTOM_LEFT; }
    public boolean isRight() { return this == TOP_RIGHT || this == BOTTOM_RIGHT; }
    public boolean isTop() { return this == TOP_LEFT || this == TOP_RIGHT; }
    public boolean isBottom() { return this == BOTTOM_LEFT || this == BOTTOM_RIGHT; }

    public int getHorizontalMult() { return this.isLeft() ? 1 : -1; }
    public int getVerticalMult() { return this.isTop() ? 1 : -1; }

    public ScreenPosition getCorner(int width,int height) { return this.corner.apply(width,height); }
}
