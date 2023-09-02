package io.github.lightman314.lightmanscurrency.common.core.variants;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

public enum Color {

    WHITE("White", 0, 0xFFFFFF, MaterialColor.SNOW, Tags.Items.DYES_WHITE),
    LIGHT_GRAY("Light Gray", 1, 0x9D9D97, MaterialColor.COLOR_LIGHT_GRAY, Tags.Items.DYES_LIGHT_GRAY, "lightgray"),
    GRAY("Gray", 2, 0x646464, MaterialColor.COLOR_GRAY, Tags.Items.DYES_GRAY),
    BLACK("Black", 3, 0x141414, MaterialColor.COLOR_BLACK, Tags.Items.DYES_BLACK),
    BROWN("Brown", 4, 0x835432, MaterialColor.COLOR_BROWN, Tags.Items.DYES_BROWN),
    RED("Red", 5, 0xFF0000, MaterialColor.COLOR_RED, Tags.Items.DYES_RED),
    ORANGE("Orange", 6, 0xFF7F00, MaterialColor.COLOR_ORANGE, Tags.Items.DYES_ORANGE),
    YELLOW("Yellow", 7, 0xFFFF00, MaterialColor.COLOR_YELLOW, Tags.Items.DYES_YELLOW),
    LIME("Lime", 8, 0x86CC26, MaterialColor.COLOR_LIGHT_GREEN, Tags.Items.DYES_LIME),
    GREEN("Green", 9, 0x007F00, MaterialColor.COLOR_GREEN, Tags.Items.DYES_GREEN),
    CYAN("Cyan", 10, 0x169B9C, MaterialColor.COLOR_CYAN, Tags.Items.DYES_CYAN),
    LIGHT_BLUE("Light Blue", 11, 0x00FFFF, MaterialColor.COLOR_LIGHT_BLUE, Tags.Items.DYES_LIGHT_BLUE, "lightblue"),
    BLUE("Blue", 12, 0x0000FF, MaterialColor.COLOR_BLUE, Tags.Items.DYES_BLUE),
    PURPLE("Purple", 13, 0x9743CD, MaterialColor.COLOR_PURPLE, Tags.Items.DYES_PURPLE),
    MAGENTA("Magenta", 14, 0xD660D1, MaterialColor.COLOR_MAGENTA, Tags.Items.DYES_MAGENTA),
    PINK("Pink", 15, 0xF4B2C9, MaterialColor.COLOR_PINK, Tags.Items.DYES_PINK);

    public final String displayName;
    private final int sortIndex;
    public final int hexColor;
    public final MaterialColor mapColor;
    public final String getResourceSafeName() { return this.toString().toLowerCase(Locale.ROOT); }
    private final String deprecatedName;
    public final boolean hasDeprecatedName() { return this.deprecatedName != null; }
    public final String getDeprecatedName() { return this.hasDeprecatedName() ? this.deprecatedName : this.toString(); }
    public final TagKey<Item> dyeTag;
    Color(@Nonnull String displayName, int sortIndex, int hexColor, @Nonnull MaterialColor mapColor, @Nonnull TagKey<Item> dyeTag, @Nullable String deprecatedName) { this.displayName = displayName; this.sortIndex = sortIndex; this.hexColor = hexColor; this.mapColor = mapColor; this.dyeTag = dyeTag; this.deprecatedName = deprecatedName; }
    Color(@Nonnull String displayName, int sortIndex, int hexColor, @Nonnull MaterialColor mapColor, @Nonnull TagKey<Item> dyeTag) { this(displayName, sortIndex, hexColor, mapColor, dyeTag, null); }

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