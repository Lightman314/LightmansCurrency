package io.github.lightman314.lightmanscurrency.common.core.variants;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.Locale;

public enum Color {

    WHITE(0, 0xFFFFFF, MapColor.SNOW, Tags.Items.DYES_WHITE),
    LIGHT_GRAY(1, 0x9D9D97, MapColor.COLOR_LIGHT_GRAY, Tags.Items.DYES_LIGHT_GRAY),
    GRAY(2, 0x646464, MapColor.COLOR_GRAY, Tags.Items.DYES_GRAY),
    BLACK(3, 0x141414, MapColor.COLOR_BLACK, Tags.Items.DYES_BLACK),
    BROWN(4, 0x835432, MapColor.COLOR_BROWN, Tags.Items.DYES_BROWN),
    RED(5, 0xFF0000, MapColor.COLOR_RED, Tags.Items.DYES_RED),
    ORANGE(6, 0xFF7F00, MapColor.COLOR_ORANGE, Tags.Items.DYES_ORANGE),
    YELLOW(7, 0xFFFF00, MapColor.COLOR_YELLOW, Tags.Items.DYES_YELLOW),
    LIME(8, 0x86CC26, MapColor.COLOR_LIGHT_GREEN, Tags.Items.DYES_LIME),
    GREEN(9, 0x007F00, MapColor.COLOR_GREEN, Tags.Items.DYES_GREEN),
    CYAN(10, 0x169B9C, MapColor.COLOR_CYAN, Tags.Items.DYES_CYAN),
    LIGHT_BLUE(11, 0x00FFFF, MapColor.COLOR_LIGHT_BLUE, Tags.Items.DYES_LIGHT_BLUE),
    BLUE(12, 0x0000FF, MapColor.COLOR_BLUE, Tags.Items.DYES_BLUE),
    PURPLE(13, 0x9743CD, MapColor.COLOR_PURPLE, Tags.Items.DYES_PURPLE),
    MAGENTA(14, 0xD660D1, MapColor.COLOR_MAGENTA, Tags.Items.DYES_MAGENTA),
    PINK(15, 0xF4B2C9, MapColor.COLOR_PINK, Tags.Items.DYES_PINK);



    public final int sortIndex;
    public final int hexColor;
    public final MapColor mapColor;
    public final String getResourceSafeName() { return this.toString().toLowerCase(Locale.ENGLISH); }
    public final String getPrettyName() {
        StringBuilder builder = new StringBuilder();
        boolean capitalize = true;
        String safeName = this.getResourceSafeName();
        for(int i = 0; i < safeName.length(); ++i)
        {
            char nextChar = safeName.charAt(i);
            if(nextChar == '_')
            {
                builder.append(' ');
                capitalize = true;
            }
            else if(capitalize)
            {
                builder.append(("" + nextChar).toUpperCase(Locale.ENGLISH));
                capitalize = false;
            }
            else
                builder.append(nextChar);
        }
        return builder.toString();
    }
    public final MutableComponent getComponent() { return EasyText.translatable("color.minecraft." + this.getResourceSafeName());}
    public final TagKey<Item> dyeTag;
    Color(int sortIndex, int hexColor, MapColor mapColor, TagKey<Item> dyeTag) { this.sortIndex = sortIndex; this.hexColor = hexColor; this.mapColor = mapColor; this.dyeTag = dyeTag; }

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
