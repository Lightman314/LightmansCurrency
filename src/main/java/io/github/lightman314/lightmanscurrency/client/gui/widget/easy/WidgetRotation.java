package io.github.lightman314.lightmanscurrency.client.gui.widget.easy;

public enum WidgetRotation {
    TOP,RIGHT,BOTTOM,LEFT;

    public WidgetRotation clockwise() {
        return switch (this) {
            case TOP -> RIGHT;
            case RIGHT -> BOTTOM;
            case BOTTOM -> LEFT;
            default -> TOP;
        };
    }

    public WidgetRotation counterClockwise() {
        return switch (this) {
            case TOP -> LEFT;
            case RIGHT -> TOP;
            case BOTTOM -> RIGHT;
            default -> BOTTOM;
        };
    }

    public static WidgetRotation fromIndex(int rotation) {
        return switch (rotation) {
            case 1 -> RIGHT;
            case 2 -> BOTTOM;
            case 3 -> LEFT;
            default -> TOP;
        };
    }
}