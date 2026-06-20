package io.github.lightman314.lightmanscurrency.api.helpers;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Comparator;
import java.util.List;

public final class ColorHelper {

    private ColorHelper() {}

    public static final List<DyeColor> SORTED_LIST = ImmutableList.of(
            DyeColor.WHITE,DyeColor.LIGHT_GRAY,DyeColor.GRAY,DyeColor.BLACK,
            DyeColor.BROWN,DyeColor.RED,DyeColor.ORANGE,DyeColor.YELLOW,
            DyeColor.LIME,DyeColor.GREEN,DyeColor.CYAN,DyeColor.LIGHT_BLUE,
            DyeColor.BLUE, DyeColor.PURPLE,DyeColor.MAGENTA,DyeColor.PINK
    );

    public static Comparator<DyeColor> COLOR_SORTER = Comparator.comparingInt(SORTED_LIST::indexOf);

    public static Block getWoolBlock(DyeColor color) {
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