package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class DirectionalSettingsWidget extends EasyWidgetWithChildren {

	private static final Map<Direction,Sprite> SPRITE_CACHE_TRUE = new HashMap<>();
	private static final Map<Direction,Sprite> SPRITE_CACHE_FALSE = new HashMap<>();

	public static final ResourceLocation BLOCK_SIDE_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "textures/gui/blocksides.png");
	
	private static final List<Direction> DIRECTIONS = Lists.newArrayList(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
	
	private static final int SPACING = 20;
	
	private final Predicate<Direction> currentValueSource;
	private final Consumer<Direction> onPress;
	final List<Direction> ignoreSides;
	
	public boolean visible = true;

	private DirectionalSettingsWidget(@Nonnull Builder builder)
	{
		super(builder);
		this.currentValueSource = builder.currentValue;
		this.onPress = builder.handler;
		this.ignoreSides = ImmutableList.copyOf(builder.ignoreSides);
	}

	@Override
	public boolean addChildrenBeforeThis() { return true; }

	@Override
	public void addChildren(@Nonnull ScreenArea area) {
		for (Direction side : DIRECTIONS) {
			if(!this.ignoreSides.contains(side))
			{
				PlainButton button = this.addChild(PlainButton.builder()
						.position(area.pos.offset(this.getSidePosX(side),this.getSidePosY(side)))
						.pressAction(() -> this.onButtonPress(side))
						.sprite(spriteForSide(side,() -> this.currentValueSource.test(side)))
						.addon(EasyAddonHelper.tooltip(InputTraderData.getFacingName(side)))
						.addon(EasyAddonHelper.visibleCheck(this::isVisible))
						.build());
			}
		}

	}

	@Override
	protected void renderWidget(@Nonnull EasyGuiGraphics gui) { }

	@Nonnull
	private static Supplier<Sprite> spriteForSide(@Nonnull Direction side, @Nonnull Supplier<Boolean> value) { return () -> getSprite(side,value.get()); }

	@Nonnull
	private static Sprite getSprite(Direction side, boolean value) {
		Map<Direction,Sprite> map = value ? SPRITE_CACHE_TRUE : SPRITE_CACHE_FALSE;
		if(!map.containsKey(side))
			map.put(side, Sprite.SimpleSprite(BLOCK_SIDE_TEXTURE, getSideU(side), value ? 32 : 0, 16, 16));
		return map.get(side);
	}
	
	private int getSidePosX(Direction side)
	{
		return switch (side) {
			case UP, SOUTH, DOWN -> SPACING;
			case EAST, NORTH -> 2 * SPACING;
			default -> 0;
		};
	}
	
	private int getSidePosY(Direction side)
	{
		return switch (side) {
			case WEST, SOUTH, EAST -> SPACING;
			case DOWN, NORTH -> 2 * SPACING;
			default -> 0;
		};
	}
	
	private static int getSideU(Direction side) { return side.get3DDataValue() * 16; }
	
	private void onButtonPress(Direction side)
	{
		this.onPress.accept(side);
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	@ParametersAreNonnullByDefault
	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(56,56); }
		@Override
		protected Builder getSelf() { return this; }

		private Predicate<Direction> currentValue = d -> false;
		private Consumer<Direction> handler = d -> {};
		private final List<Direction> ignoreSides = new ArrayList<>();

		public Builder currentValue(Predicate<Direction> currentValue) { this.currentValue = currentValue; return this; }
		public Builder handler(Consumer<Direction> handler) { this.handler = handler; return this; }
		public Builder ignore(Direction direction) { if(!this.ignoreSides.contains(direction)) this.ignoreSides.add(direction); return this; }
		public Builder ignore(Iterable<Direction> directions) { for(Direction d : directions) this.ignore(d); return this; }

		public DirectionalSettingsWidget build() { return new DirectionalSettingsWidget(this); }

	}
	
}
