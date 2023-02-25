package io.github.lightman314.lightmanscurrency.client.util;

import java.util.function.BiFunction;

public enum ScreenCorner
{
    TOP_LEFT(false, false, (w,h) -> ScreenPosition.of(0,0)),
    TOP_RIGHT(true, false, (w,h) -> ScreenPosition.of(w,0)),
    BOTTOM_LEFT(false, true, (w,h) -> ScreenPosition.of(0,h)),
    BOTTOM_RIGHT(true, true, ScreenPosition::of);

    public final boolean isRightSide;
    public final boolean isBottomSide;
    private final BiFunction<Integer,Integer,ScreenPosition> corner;
    ScreenCorner(boolean isRightSide, boolean isBottomSide, BiFunction<Integer,Integer,ScreenPosition> corner) { this.isRightSide = isRightSide; this.isBottomSide = isBottomSide; this.corner = corner; }

    public ScreenPosition getCorner(int screenWidth, int screenHeight) { return this.corner.apply(screenWidth, screenHeight); }

}
