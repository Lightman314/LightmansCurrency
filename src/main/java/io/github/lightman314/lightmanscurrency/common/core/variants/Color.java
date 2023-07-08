package io.github.lightman314.lightmanscurrency.common.core.variants;

import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.Locale;

public enum Color {

    WHITE(0, 0xFFFFFF, MapColor.SNOW),
    LIGHT_GRAY(1, 0x9D9D97, MapColor.COLOR_LIGHT_GRAY),
    GRAY(2, 0x646464, MapColor.COLOR_GRAY),
    BLACK(3, 0x141414, MapColor.COLOR_BLACK),
    BROWN(4, 0x835432, MapColor.COLOR_BROWN),
    RED(5, 0xFF0000, MapColor.COLOR_RED),
    ORANGE(6, 0xFF7F00, MapColor.COLOR_ORANGE),
    YELLOW(7, 0xFFFF00, MapColor.COLOR_YELLOW),
    LIME(8, 0x86CC26, MapColor.COLOR_LIGHT_GREEN),
    GREEN(9, 0x007F00, MapColor.COLOR_GREEN),
    CYAN(10, 0x169B9C, MapColor.COLOR_CYAN),
    LIGHT_BLUE(11, 0x00FFFF, MapColor.COLOR_LIGHT_BLUE),
    BLUE(12, 0x0000FF, MapColor.COLOR_BLUE),
    PURPLE(13, 0x9743CD, MapColor.COLOR_PURPLE),
    MAGENTA(14, 0xD660D1, MapColor.COLOR_MAGENTA),
    PINK(15, 0xF4B2C9, MapColor.COLOR_PINK);

    private final int sortIndex;
    public final int hexColor;
    public final MapColor mapColor;
    public final String getResourceSafeName() { return this.toString().toLowerCase(Locale.ENGLISH); }
    Color(int sortIndex, int hexColor, MapColor mapColor) { this.sortIndex = sortIndex; this.hexColor = hexColor; this.mapColor = mapColor; }

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
