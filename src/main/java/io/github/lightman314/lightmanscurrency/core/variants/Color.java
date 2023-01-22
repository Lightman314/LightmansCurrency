package io.github.lightman314.lightmanscurrency.core.variants;

public enum Color {

    WHITE(0, 0xFFFFFF), LIGHTGRAY(1, 0x9D9D97), GRAY(2, 0x646464), BLACK(3, 0x141414), BROWN(4, 0x835432), RED(5, 0xFF0000), ORANGE(6, 0xFF7F00), YELLOW(7, 0xFFFF00), LIME(8, 0x86CC26), GREEN(9, 0x007F00), CYAN(10, 0x169B9C), LIGHTBLUE(11, 0x00FFFF), BLUE(12, 0x0000FF), PURPLE(13, 0x9743CD), MAGENTA(14, 0xD660D1), PINK(15, 0xF4B2C9);
    private final int sortIndex;
    public final int hexColor;
    Color(int sortIndex, int hexColor) { this.sortIndex = sortIndex; this.hexColor = hexColor; }

    public static Color getFromIndex(long index) {
        index = index % 16;
        for(Color c : values())
        {
            if(c.sortIndex == index)
                return c;
        }
        return WHITE;
    }

    public static int sortByColor(Color c1, Color c2) { return Integer.compare(c1.sortIndex, c2.sortIndex); }

}