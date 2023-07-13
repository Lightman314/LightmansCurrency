package io.github.lightman314.lightmanscurrency.common.core.variants;

import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public enum Color {



    WHITE(0, 0xFFFFFF, MaterialColor.SNOW),
    LIGHT_GRAY(1, 0x9D9D97, MaterialColor.COLOR_LIGHT_GRAY, "lightgray"),
    GRAY(2, 0x646464, MaterialColor.COLOR_GRAY),
    BLACK(3, 0x141414, MaterialColor.COLOR_BLACK),
    BROWN(4, 0x835432, MaterialColor.COLOR_BROWN),
    RED(5, 0xFF0000, MaterialColor.COLOR_RED),
    ORANGE(6, 0xFF7F00, MaterialColor.COLOR_ORANGE),
    YELLOW(7, 0xFFFF00, MaterialColor.COLOR_YELLOW),
    LIME(8, 0x86CC26, MaterialColor.COLOR_LIGHT_GREEN),
    GREEN(9, 0x007F00, MaterialColor.COLOR_GREEN),
    CYAN(10, 0x169B9C, MaterialColor.COLOR_CYAN),
    LIGHT_BLUE(11, 0x00FFFF, MaterialColor.COLOR_LIGHT_BLUE, "lightblue"),
    BLUE(12, 0x0000FF, MaterialColor.COLOR_BLUE),
    PURPLE(13, 0x9743CD, MaterialColor.COLOR_PURPLE),
    MAGENTA(14, 0xD660D1, MaterialColor.COLOR_MAGENTA),
    PINK(15, 0xF4B2C9, MaterialColor.COLOR_PINK);



    private final int sortIndex;
    public final int hexColor;
    public final MaterialColor mapColor;
    public final String getResourceSafeName() { return this.toString().toLowerCase(Locale.ENGLISH); }
    private final String deprecatedName;
    public final boolean hasDeprecatedName() { return this.deprecatedName != null; }
    public final String getDeprecatedName() { return this.hasDeprecatedName() ? this.deprecatedName : this.toString(); }
    Color(int sortIndex, int hexColor, MaterialColor mapColor, String deprecatedName) { this.sortIndex = sortIndex; this.hexColor = hexColor; this.mapColor = mapColor; this.deprecatedName = deprecatedName; }
    Color(int sortIndex, int hexColor, MaterialColor mapColor) { this(sortIndex, hexColor, mapColor, null); }

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

    private static List<Color> deprecatedValues = null;

    public static Collection<Color> deprecatedValues()
    {
        if(deprecatedValues == null)
        {
            deprecatedValues = new ArrayList<>();
            for(Color c : values())
            {
                if(c.hasDeprecatedName())
                    deprecatedValues.add(c);
            }
        }
        return deprecatedValues;
    }

}
