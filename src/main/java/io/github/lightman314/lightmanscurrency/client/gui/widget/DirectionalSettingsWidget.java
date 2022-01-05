package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.trader.settings.directional.DirectionalSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class DirectionalSettingsWidget {

	public static final ResourceLocation BLOCK_SIDE_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/blocksides.png");
	
	private static final List<Direction> DIRECTIONS = Lists.newArrayList(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
	
	private static final int SPACING = 20;
	
	private final Supplier<DirectionalSettings> settingSource;
	private final Consumer<Direction> onPress;
	List<PlainButton> directionButtons;
	
	public DirectionalSettingsWidget(int x, int y, Supplier<DirectionalSettings> settingSource, Consumer<Direction> onPress, Consumer<Button> addButton)
	{
		//DOWN, UP, NORTH, SOUTH, WEST, EAST
		this.settingSource = settingSource;
		this.onPress = onPress;
		this.directionButtons = Lists.newArrayListWithCapacity(Direction.values().length);
		
		for(int i = 0; i < DIRECTIONS.size(); ++i)
		{
			Direction side = DIRECTIONS.get(i);
			boolean value = this.settingSource.get().get(side);
			PlainButton button = new PlainButton(x + this.getSidePosX(side), y + this.getSidePosY(side), 16, 16, this::onButtonPress, BLOCK_SIDE_TEXTURE, this.getSideU(side), value ? 32 : 0);
			button.visible = this.settingSource.get().allows(side);
			this.directionButtons.add(button);
			addButton.accept(button);
		}
		
	}
	
	public void renderTooltips(MatrixStack matrix, int mouseX, int mouseY, Screen screen)
	{
		for(Direction side : Direction.values())
		{
			Button button = this.getButton(side);
			if(button.isMouseOver(mouseX, mouseY))
				screen.renderTooltip(matrix, new TranslationTextComponent("gui.lightmanscurrency.settings.side." + side.toString().toLowerCase()), mouseX, mouseY);
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
		return side.getIndex() * 16;
	}
	
	public PlainButton getButton(Direction direction)
	{
		return this.directionButtons.get(direction.getIndex());
	}
	
	public void tick() {
		DirectionalSettings settings = this.settingSource.get();
		for(Direction side : Direction.values())
		{
			PlainButton button = this.getButton(side);
			button.visible = settings.allows(side);
			button.setResource(BLOCK_SIDE_TEXTURE, this.getSideU(side), settings.get(side) ? 32 : 0);
		}
	}
	
	private void onButtonPress(Button button)
	{
		int index = this.directionButtons.indexOf(button);
		if(index < 0)
			return;
		this.onPress.accept(Direction.byIndex(index));
	}
	
}
