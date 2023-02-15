package io.github.lightman314.lightmanscurrency.core.variants;

import javax.annotation.Nullable;

public enum Color {

    WHITE(0, 0xFFFFFF),
    LIGHT_GRAY(1, 0x9D9D97, "LIGHTGRAY"),
    GRAY(2, 0x646464),
    BLACK(3, 0x141414),
    BROWN(4, 0x835432),
    RED(5, 0xFF0000),
    ORANGE(6, 0xFF7F00),
    YELLOW(7, 0xFFFF00),
    LIME(8, 0x86CC26),
    GREEN(9, 0x007F00),
    CYAN(10, 0x169B9C),
    LIGHT_BLUE(11, 0x00FFFF, "LIGHTBLUE"),
    BLUE(12, 0x0000FF),
    PURPLE(13, 0x9743CD),
    MAGENTA(14, 0xD660D1),
    PINK(15, 0xF4B2C9);

    private final int sortIndex;
    public final int hexColor;
    private final String oldName;
    public final String getOldName() { return this.oldName == null ? this.toString() : this.oldName; }
    Color(int sortIndex, int hexColor) { this.sortIndex = sortIndex; this.hexColor = hexColor; this.oldName = null; }
    Color(int sortIndex, int hexColor, String oldName) { this.sortIndex = sortIndex; this.hexColor = hexColor; this.oldName = oldName; }

    public static Color getFromIndex(long index) {
        index = index % 16;
        for(Color c : values())
        {
            if(c.sortIndex == index)
                return c;
        }
        return WHITE;
    }

    @Nullable
    public static Color getFromPrettyName(String name) {
        for(Color c : values())
        {
            if(c.toString().equalsIgnoreCase(name))
                return c;
        }
        return null;
    }

    public static int sortByColor(Color c1, Color c2) { return Integer.compare(c1.sortIndex, c2.sortIndex); }

}