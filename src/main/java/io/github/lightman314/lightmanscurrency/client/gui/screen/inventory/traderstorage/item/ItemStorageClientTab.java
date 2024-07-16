package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemStorageClientTab extends TraderStorageClientTab<ItemStorageTab> implements IScrollable, IMouseListener {

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int COLUMNS_NORMAL = 8;
	private static final int COLUMNS_PERSISTENT = 10;
	private static final int ROWS = 5;
	
	public ItemStorageClientTab(Object screen, ItemStorageTab commonTab) { super(screen, commonTab); }

	int scroll = 0;
	
	ScrollBarWidget scrollBar;

	int columns = COLUMNS_NORMAL;

	@Nonnull
	@Override
	public IconData getIcon() { return IconUtil.ICON_STORAGE; }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_STORAGE.get(); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.addChild(this);

		this.columns = COLUMNS_NORMAL;
		if(this.menu.getTrader() instanceof ItemTraderData trader && trader.isPersistent())
			this.columns = COLUMNS_PERSISTENT;

		this.scrollBar = this.addChild(new ScrollBarWidget(screenArea.pos.offset(X_OFFSET + (18 * this.columns), Y_OFFSET), ROWS * 18, this));
		
		this.addChild(new ScrollListener(screenArea.pos, screenArea.width, 118, this));

		if(this.menu.getTrader() instanceof ItemTraderData trader && !trader.isPersistent())
		{
			this.addChild(IconAndButtonUtil.quickInsertButton(screenArea.pos.offset(22, Y_OFFSET + 18 * ROWS + 8), b -> this.commonTab.quickTransfer(0)));
			this.addChild(IconAndButtonUtil.quickExtractButton(screenArea.pos.offset(34, Y_OFFSET + 18 * ROWS + 8), b -> this.commonTab.quickTransfer(1)));
		}
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		gui.drawString(LCText.TOOLTIP_TRADER_STORAGE.get(), 8, 6, 0x404040);
		
		if(this.menu.getTrader() instanceof ItemTraderData trader)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each display slot
			int index = this.scroll * this.columns;
			TraderItemStorage storage = trader.getStorage();
			int hoverSlot = this.isMouseOverSlot(gui.mousePos) + (this.scroll * this.columns);
			for(int y = 0; y < ROWS; ++y)
			{
				int yPos = Y_OFFSET + y * 18;
				for(int x = 0; x < this.columns; ++x)
				{
					//Get the slot position
					int xPos = X_OFFSET + x * 18;
					//Render the slot background
					gui.resetColor();
					gui.blit(TraderScreen.GUI_TEXTURE, xPos, yPos, TraderScreen.WIDTH, 0, 18, 18);
					//Render the slots item
					if(index < storage.getSlotCount())
						gui.renderItem(storage.getContents().get(index), xPos + 1, yPos + 1, this.getCountText(storage.getContents().get(index)));
					if(index == hoverSlot)
						gui.renderSlotHighlight(xPos + 1, yPos + 1);
					index++;
				}
			}
			
			//Render the slot bg for the upgrade slots
			gui.resetColor();
			for(Slot slot : this.commonTab.getSlots())
				gui.blit(TraderScreen.GUI_TEXTURE, slot.x - 1, slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);
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

	@Override
	public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		
		if(this.menu.getTrader() instanceof ItemTraderData && this.screen.getMenu().getHeldItem().isEmpty())
		{
			int hoveredSlot = this.isMouseOverSlot(gui.mousePos);
			if(hoveredSlot >= 0)
			{
				hoveredSlot += scroll * this.columns;
				TraderItemStorage storage = ((ItemTraderData)this.menu.getTrader()).getStorage();
				if(hoveredSlot < storage.getContents().size())
				{
					ItemStack stack = storage.getContents().get(hoveredSlot);
					if(stack.isEmpty())
						return;
					EasyScreenHelper.RenderItemTooltipWithCount(gui, stack, storage.getMaxAmount(), ChatFormatting.YELLOW);
				}	
			}
		}
	}
	
	private int isMouseOverSlot(ScreenPosition mousePos) {
		
		int foundColumn = -1;
		int foundRow = -1;
		
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;
		for(int x = 0; x < this.columns && foundColumn < 0; ++x)
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
		return (foundRow * this.columns) + foundColumn;
	}
	
	private int totalStorageSlots() {
		if(this.menu.getTrader() instanceof ItemTraderData)
		{
			return ((ItemTraderData)this.menu.getTrader()).getStorage().getContents().size();
		}
		return 0;
	}
	
	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		
		if(this.menu.getTrader() instanceof ItemTraderData)
		{
			int hoveredSlot = this.isMouseOverSlot(ScreenPosition.of(mouseX, mouseY));
			if(hoveredSlot >= 0)
			{
				hoveredSlot += this.scroll * this.columns;
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
	public int getMaxScroll() { return Math.max(((this.totalStorageSlots() - 1) / this.columns) - ROWS + 1, 0); }
	
}
