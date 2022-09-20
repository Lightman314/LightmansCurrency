package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class DirectionalSettingsWidget {

	public static final ResourceLocation BLOCK_SIDE_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/blocksides.png");
	
	private static final List<Direction> DIRECTIONS = Lists.newArrayList(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
	
	private static final int SPACING = 20;
	
	private final Function<Direction,Boolean> currentValueSource;
	private final Consumer<Direction> onPress;
	List<PlainButton> directionButtons;
	
	public boolean visible = true;
	
	public DirectionalSettingsWidget(int x, int y, Function<Direction,Boolean> currentValueSource, ImmutableList<Direction> ignoreSides, Consumer<Direction> onPress, Consumer<Button> addButton)
	{
		//DOWN, UP, NORTH, SOUTH, WEST, EAST
		this.currentValueSource = currentValueSource;
		this.onPress = onPress;
		this.directionButtons = Lists.newArrayListWithCapacity(Direction.values().length);
		
		for(int i = 0; i < DIRECTIONS.size(); ++i)
		{
			Direction side = DIRECTIONS.get(i);
			boolean value = this.currentValueSource.apply(side);
			PlainButton button = new PlainButton(x + this.getSidePosX(side), y + this.getSidePosY(side), 16, 16, this::onButtonPress, BLOCK_SIDE_TEXTURE, this.getSideU(side), value ? 32 : 0);
			button.visible = !ignoreSides.contains(side);
			this.directionButtons.add(button);
			addButton.accept(button);
		}
		
	}
	
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY, Screen screen)
	{
		for(Direction side : Direction.values())
		{
			Button button = this.getButton(side);
			if(button.isMouseOver(mouseX, mouseY))
				screen.renderTooltip(pose, InputTraderData.getFacingName(side), mouseX, mouseY);
		}
	}
	
	private int getSidePosX(Direction side)
	{
		switch(side)
		{
		case UP:
		case SOUTH:
		case DOWN:
			return SPACING;
		case EAST:
		case NORTH:
			return 2 * SPACING;
			default:
				return 0;
		}
	}
	
	private int getSidePosY(Direction side)
	{
		switch(side)
		{
		case WEST:
		case SOUTH:
		case EAST:
			return SPACING;
		case DOWN:
		case NORTH:
			return 2 * SPACING;
		default:
			return 0;
		}
	}
	
	private int getSideU(Direction side)
	{
		return side.get3DDataValue() * 16;
	}
	
	public PlainButton getButton(Direction direction)
	{
		return this.directionButtons.get(direction.get3DDataValue());
	}
	
	public void tick() {
		for(Direction side : Direction.values())
		{
			PlainButton button = this.getButton(side);
			button.setResource(BLOCK_SIDE_TEXTURE, this.getSideU(side), this.currentValueSource.apply(side) ? 32 : 0);
		}
	}
	
	private void onButtonPress(Button button)
	{
		int index = this.directionButtons.indexOf(button);
		if(index < 0)
			return;
		this.onPress.accept(Direction.from3DDataValue(index));
	}
	
}