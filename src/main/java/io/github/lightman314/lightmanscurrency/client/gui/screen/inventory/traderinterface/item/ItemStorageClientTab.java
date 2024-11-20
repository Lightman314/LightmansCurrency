package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.item;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.blockentity.ItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.ConfigurableSidedHandler.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.item.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemStorageClientTab extends TraderInterfaceClientTab<ItemStorageTab> implements IScrollable, IMouseListener {

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int COLUMNS = 8;
	private static final int ROWS = 2;

	private static final int WIDGET_OFFSET = Y_OFFSET + 18 * ROWS + 4;

	DirectionalSettingsWidget inputSettings;
	DirectionalSettingsWidget outputSettings;

	public ItemStorageClientTab(Object screen, ItemStorageTab commonTab) { super(screen, commonTab); }

	int scroll = 0;

	ScrollBarWidget scrollBar;

	@Nonnull
	@Override
	public IconData getIcon() { return IconUtil.ICON_STORAGE; }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_INTERFACE_STORAGE.get(); }

	private DirectionalSettings getInputSettings() {
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
			return ((ItemTraderInterfaceBlockEntity)this.menu.getBE()).getItemHandler().getInputSides();
		return new DirectionalSettings();
	}

	private DirectionalSettings getOutputSettings() {
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity be)
			return be.getItemHandler().getOutputSides();
		return new DirectionalSettings();
	}

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.scrollBar = this.addChild(ScrollBarWidget.builder()
				.position(screenArea.pos.offset(X_OFFSET + (18 * COLUMNS),Y_OFFSET))
				.height(ROWS * 18)
				.scrollable(this)
				.build());

		this.addChild(ScrollListener.builder()
				.position(screenArea.pos)
				.size(screenArea.width,118)
				.listener(this)
				.build());

		this.inputSettings = this.addChild(DirectionalSettingsWidget.builder()
				.position(screenArea.pos.offset(33,WIDGET_OFFSET + 9))
				.currentValue(this.getInputSettings()::get)
				.ignore(this.getInputSettings().ignoreSides)
				.handler(this::ToggleInputSide)
				.build());
		this.outputSettings = this.addChild(DirectionalSettingsWidget.builder()
				.position(screenArea.pos.offset(116,WIDGET_OFFSET + 9))
				.currentValue(this.getOutputSettings()::get)
				.ignore(this.getOutputSettings().ignoreSides)
				.handler(this::ToggleOutputSide)
				.build());

		this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(22,Y_OFFSET + 18 * 5 + 8))
				.pressAction(() -> this.commonTab.quickTransfer(0))
				.sprite(IconAndButtonUtil.SPRITE_QUICK_INSERT)
				.build());
		this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(34, Y_OFFSET + 18 * 5 + 8))
				.pressAction(() -> this.commonTab.quickTransfer(1))
				.sprite(IconAndButtonUtil.SPRITE_QUICK_EXTRACT)
				.build());

	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity be)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each display slot
			int index = this.scroll * COLUMNS;
			TraderItemStorage storage = be.getItemBuffer();
			int hoveredSlot = this.isMouseOverSlot(gui.mousePos) + (this.scroll * COLUMNS);
			for(int y = 0; y < ROWS; ++y)
			{
				int yPos = Y_OFFSET + y * 18;
				for(int x = 0; x < COLUMNS; ++x)
				{
					//Get the slot position
					int xPos = X_OFFSET + x * 18;
					//Render the slot background
					gui.resetColor();
					gui.blit(TraderInterfaceScreen.GUI_TEXTURE, xPos, yPos, TraderInterfaceScreen.WIDTH, 0, 18, 18);
					//Render the slots item
					if(index < storage.getSlotCount())
						gui.renderItem(storage.getContents().get(index), xPos + 1, yPos + 1, this.getCountText(storage.getContents().get(index)));
					if(index == hoveredSlot)
						gui.renderSlotHighlight(xPos + 1, yPos + 1);
					index++;
				}
			}

			//Render the slot bg for the upgrade slots
			gui.resetColor();
			for(Slot slot : this.commonTab.getSlots())
				gui.blit(TraderInterfaceScreen.GUI_TEXTURE, slot.x - 1, slot.y - 1, TraderInterfaceScreen.WIDTH, 0, 18, 18);

			//Render the input/output labels
			gui.drawString(LCText.GUI_SETTINGS_INPUT_SIDE.get(), 33, WIDGET_OFFSET, 0x404040);
			Component outputText = LCText.GUI_SETTINGS_OUTPUT_SIDE.get();
			gui.drawString(outputText, 173 - gui.font.width(outputText), WIDGET_OFFSET, 0x404040);
		}

	}

	private String getCountText(ItemStack stack) {
		int count = stack.getCount();
		if(count <= 1)
			return null;
		if(count >= 1000)
		{
			String countText = String.valueOf(count / 1000);
			if((count % 1000) / 100 > 0)
				countText += "." + ((count % 1000) / 100);
			return countText + "k";
		}
		return String.valueOf(count);
	}


	public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {

		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
		{
			if(this.screen.getMenu().getCarried().isEmpty())
			{
				int hoveredSlot = this.isMouseOverSlot(gui.mousePos);
				if(hoveredSlot >= 0)
				{
					hoveredSlot += scroll * COLUMNS;
					TraderItemStorage storage = ((ItemTraderInterfaceBlockEntity)this.menu.getBE()).getItemBuffer();
					if(hoveredSlot < storage.getContents().size())
					{
						ItemStack stack = storage.getContents().get(hoveredSlot);
						if(stack.isEmpty())
							return;
						//Vanilla tooltip
						EasyScreenHelper.RenderItemTooltipWithCount(gui, stack, storage.getMaxAmount(), ChatFormatting.YELLOW);
					}
				}
			}
		}
	}

	private int isMouseOverSlot(ScreenPosition mousePos) {

		int foundColumn = -1;
		int foundRow = -1;

		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;
		for(int x = 0; x < COLUMNS && foundColumn < 0; ++x)
		{
			if(mousePos.x >= leftEdge + x * 18 && mousePos.x < leftEdge + (x * 18) + 18)
				foundColumn = x;
		}
		for(int y = 0; y < ROWS && foundRow < 0; ++y)
		{
			if(mousePos.y >= topEdge + y * 18 && mousePos.y < topEdge + (y * 18) + 18)
				foundRow = y;
		}
		if(foundColumn < 0 || foundRow < 0)
			return -1;
		return (foundRow * COLUMNS) + foundColumn;
	}

	private int totalStorageSlots() {
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
		{
			return ((ItemTraderInterfaceBlockEntity)this.menu.getBE()).getItemBuffer().getContents().size();
		}
		return 0;
	}

	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {

		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
		{
			int hoveredSlot = this.isMouseOverSlot(ScreenPosition.of(mouseX, mouseY));
			if(hoveredSlot >= 0)
			{
				hoveredSlot += this.scroll * COLUMNS;
				this.commonTab.clickedOnSlot(hoveredSlot, Screen.hasShiftDown(), button == 0);
				return true;
			}
		}
		return false;
	}

	@Override
	public int currentScroll() { return this.scroll; }

	@Override
	public void setScroll(int newScroll) {
		this.scroll = newScroll;
		this.validateScroll();
	}

	@Override
	public int getMaxScroll() {
		return Math.max(((this.totalStorageSlots() - 1) / COLUMNS) - ROWS + 1, 0);
	}

	private void ToggleInputSide(Direction side) {
		this.commonTab.toggleInputSlot(side);
	}

	private void ToggleOutputSide(Direction side) {
		this.commonTab.toggleOutputSlot(side);
	}

	@Nullable
	@Override
	public Pair<ItemStack, ScreenArea> getHoveredItem(@Nonnull ScreenPosition mousePos) {
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity be) {
			int foundColumn = -1;
			int foundRow = -1;

			int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
			int topEdge = this.screen.getGuiTop() + Y_OFFSET;
			for(int x = 0; x < COLUMNS && foundColumn < 0; ++x)
			{
				if(mousePos.x >= leftEdge + x * 18 && mousePos.x < leftEdge + (x * 18) + 18)
					foundColumn = x;
			}
			for(int y = 0; y < ROWS && foundRow < 0; ++y)
			{
				if(mousePos.y >= topEdge + y * 18 && mousePos.y < topEdge + (y * 18) + 18)
					foundRow = y;
			}
			if(foundColumn < 0 || foundRow < 0)
				return null;
			int slot = (foundRow * COLUMNS) + foundColumn + (this.scroll * COLUMNS);
			ItemStack stack = be.getItemBuffer().getStackInSlot(slot);
			return Pair.of(stack,ScreenArea.of(leftEdge + (foundColumn * 18),topEdge + (foundRow * 18),18,18));
		}
		return null;
	}

}