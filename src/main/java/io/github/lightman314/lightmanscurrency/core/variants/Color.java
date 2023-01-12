package io.github.lightman314.lightmanscurrency.core.variants;

public enum Color {

    WHITE(0), LIGHTGRAY(1), GRAY(2), BLACK(3), BROWN(4), RED(5), ORANGE(6), YELLOW(7), LIME(8), GREEN(9), CYAN(10), LIGHTBLUE(11), BLUE(12), PURPLE(13), MAGENTA(14), PINK(15);
    private final int sortIndex;
    Color(int sortIndex) { this.sortIndex = sortIndex; }

    public static int sortByColor(Color c1, Color c2) { return Integer.compare(c1.sortIndex, c2.sortIndex); }

}
