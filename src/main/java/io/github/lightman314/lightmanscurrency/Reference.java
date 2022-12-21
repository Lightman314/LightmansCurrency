package io.github.lightman314.lightmanscurrency;

import com.google.common.collect.ImmutableList;

public class Reference {

	public enum Color { WHITE, ORANGE, MAGENTA, LIGHTBLUE, YELLOW, LIME, PINK, GRAY, LIGHTGRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK }
	public enum WoodType { OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK, MANGROVE, CRIMSON, WARPED }

	public static final ImmutableList<Color> SORTED_COLORS = ImmutableList.of(Color.WHITE, Color.LIGHTGRAY, Color.GRAY, Color.BLACK, Color.BROWN, Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.GREEN, Color.CYAN, Color.LIGHTBLUE, Color.BLUE, Color.PURPLE, Color.MAGENTA, Color.PINK);
	public static final ImmutableList<WoodType> SORTED_WOODS = ImmutableList.of(WoodType.OAK, WoodType.SPRUCE, WoodType.BIRCH, WoodType.JUNGLE, WoodType.ACACIA, WoodType.DARK_OAK, WoodType.MANGROVE, WoodType.CRIMSON, WoodType.WARPED);

	public static int sortByColor(Color c1, Color c2) { return Integer.compare(SORTED_COLORS.indexOf(c1), SORTED_COLORS.indexOf(c2)); }

	public static int sortByWood(WoodType w1, WoodType w2) { return Integer.compare(SORTED_WOODS.indexOf(w1), SORTED_WOODS.indexOf(w2)); }

}
