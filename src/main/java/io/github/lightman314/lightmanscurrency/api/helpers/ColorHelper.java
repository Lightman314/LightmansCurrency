package io.github.lightman314.lightmanscurrency.api.helpers;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Comparator;
import java.util.List;

public final class ColorHelper {

    private ColorHelper() {}

    public static final List<VanillaColor> SORTED_LIST = ImmutableList.of(
            VanillaColor.WHITE,VanillaColor.LIGHT_GRAY,VanillaColor.GRAY,VanillaColor.BLACK,
            VanillaColor.BROWN,VanillaColor.RED,VanillaColor.ORANGE,VanillaColor.YELLOW,
            VanillaColor.LIME,VanillaColor.GREEN,VanillaColor.CYAN,VanillaColor.LIGHT_BLUE,
            VanillaColor.BLUE, VanillaColor.PURPLE,VanillaColor.MAGENTA,VanillaColor.PINK
    );

    public static Comparator<VanillaColor> COLOR_SORTER = Comparator.comparingInt(SORTED_LIST::indexOf);

    public static Block getWoolBlock(VanillaColor color) {
        return switch (color) {
            case WHITE -> Blocks.WHITE_WOOL;
            case ORANGE -> Blocks.ORANGE_WOOL;
            case MAGENTA -> Blocks.MAGENTA_WOOL;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
            case YELLOW -> Blocks.YELLOW_WOOL;
            case LIME -> Blocks.LIME_WOOL;
            case PINK -> Blocks.PINK_WOOL;
            case GRAY -> Blocks.GRAY_WOOL;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
            case CYAN -> Blocks.CYAN_WOOL;
            case PURPLE -> Blocks.PURPLE_WOOL;
            case BLUE -> Blocks.BLUE_WOOL;
            case BROWN -> Blocks.BROWN_WOOL;
            case GREEN -> Blocks.GREEN_WOOL;
            case RED -> Blocks.RED_WOOL;
            case BLACK -> Blocks.BLACK_WOOL;
        };
    }

}