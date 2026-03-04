package io.github.lightman314.lightmanscurrency.common.traders.auction.client.tabs;

import java.util.List;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.client.gui.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.nodes.AuctionStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tabs.AuctionStorageTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class AuctionStorageClientTab extends TraderStorageClientTab<AuctionStorageTab> implements IScrollable, IMouseListener {

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int COLUMNS = 10;
	private static final int ROWS = 4;
	
	public AuctionStorageClientTab(Object screen, AuctionStorageTab commonTab) { super(screen, commonTab); }
	
	int scroll = 0;
	
	ScrollBarWidget scrollBar;
	
	EasyButton buttonCollectItems;
	
	IconButton buttonCollectMoney;

	@Override
	public IconData getIcon() { return IconUtil.ICON_STORAGE; }
	
	@Override
	public Component getTooltip() { return LCText.TOOLTIP_TRADER_AUCTION_STORAGE.get(); }

    @Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.scrollBar = this.addChild(ScrollBarWidget.builder()
				.position(screenArea.pos.offset(X_OFFSET + (18 * COLUMNS),Y_OFFSET))
				.height(ROWS * 18)
				.scrollable(this)
				.build());
		
		this.buttonCollectItems = this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(11, Y_OFFSET + 18 * ROWS + 8))
				.pressAction(this.commonTab::quickTransfer)
				.sprite(SpriteUtil.BUTTON_QUICK_EXTRACT)
				.build());

		this.buttonCollectMoney = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(25,118))
				.pressAction(this.commonTab::collectCoins)
				.icon(IconUtil.ICON_COLLECT_COINS)
				.build());
		
		this.addChild(ScrollListener.builder()
				.position(screenArea.pos)
				.size(screenArea.width,118)
				.listener(this)
				.build());
		
	}
	
	@Override
	public void renderBG(EasyGuiGraphics gui) {

		gui.drawString(LCText.TOOLTIP_TRADER_AUCTION_STORAGE.get(), 8, 6, 0x404040);

        AuctionStorageNode node = this.commonTab.getNode();
		if(node != null)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each display slot
			int index = this.scroll * COLUMNS;
			AuctionPlayerStorage storage = node.getStorage(this.screen.getPlayer());
			if(storage != null)
			{
				List<ItemStack> storedItems = storage.getStoredItems();
				int hoverSlot = this.isMouseOverSlot(gui.mousePos) + (this.scroll * COLUMNS);
				for(int y = 0; y < ROWS && index < storedItems.size(); ++y)
				{
					int yPos = Y_OFFSET + y * 18;
					for(int x = 0; x < COLUMNS && index < storedItems.size(); ++x)
					{
						//Get the slot position
						int xPos = X_OFFSET + x * 18;
						//Render the slot background
						gui.resetColor();
                        SpriteUtil.EMPTY_SLOT_NORMAL.render(gui,xPos,yPos);
						//Render the slots item
						if(index < storedItems.size())
							gui.renderItem(storedItems.get(index), xPos + 1, yPos + 1);
						if(index == hoverSlot)
							gui.renderSlotHighlight(xPos + 1, yPos + 1);
						index++;
					}
				}
				
				if(storedItems.isEmpty())
					TextRenderUtil.drawCenteredMultilineText(gui, LCText.GUI_TRADER_AUCTION_STORAGE_ITEMS_NONE.get(), 10, this.screen.getXSize() - 20, X_OFFSET + (18 * ROWS / 2), 0x404040);
				
				this.buttonCollectItems.active = !storedItems.isEmpty();
				
				//Render the stored money amount
				if(!storage.getStoredCoins().isEmpty())
				{
					this.buttonCollectMoney.active = true;
					gui.drawString(LCText.GUI_TRADER_AUCTION_STORAGE_MONEY.get(storage.getStoredCoins().getRandomValueText()), 50, 118, 0x404040);
				}
				else
				{
					this.buttonCollectMoney.active = false;
					gui.drawString(LCText.GUI_TRADER_AUCTION_STORAGE_MONEY_NONE.get(), 50, 118, 0x404040);
				}
				
			}
			
		}
		
	}
	
	@Override
	public void renderAfterWidgets(EasyGuiGraphics gui) {

        AuctionStorageNode node = this.commonTab.getNode();
		if(node != null && this.screen.getMenu().getHeldItem().isEmpty())
		{
			int hoveredSlot = this.isMouseOverSlot(gui.mousePos);
			if(hoveredSlot >= 0)
			{
				hoveredSlot += scroll * COLUMNS;
				AuctionPlayerStorage storage = node.getStorage(this.screen.getPlayer());
				if(hoveredSlot < storage.getStoredItems().size())
				{
					ItemStack stack = storage.getStoredItems().get(hoveredSlot);
					gui.renderComponentTooltip(EasyScreenHelper.getTooltipFromItem(stack));
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
        AuctionStorageNode node = this.commonTab.getNode();
		if(node != null)
			return node.getStorage(this.screen.getPlayer()).getStoredItems().size();
		return 0;
	}
	
	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		if(this.menu.getTrader() instanceof AuctionHouseTrader)
		{
			int hoveredSlot = this.isMouseOverSlot(ScreenPosition.of(mouseX, mouseY));
			if(hoveredSlot >= 0)
			{
				hoveredSlot += this.scroll * COLUMNS;
				this.commonTab.clickedOnSlot(hoveredSlot, Screen.hasShiftDown());
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

	@Nullable
	@Override
	public Pair<ItemStack, ScreenArea> getHoveredItem(ScreenPosition mousePos) {

        AuctionStorageNode node = this.commonTab.getNode();
		if(node != null)
        {
			AuctionPlayerStorage storage = node.getStorage(this.menu.getPlayer());
			if(storage == null)
				return null;

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
			if(slot >= storage.getStoredItems().size())
				return null;
			ItemStack stack = storage.getStoredItems().get(slot);
			return Pair.of(stack,ScreenArea.of(leftEdge + (foundColumn * 18),topEdge + (foundRow * 18),18,18));
		}
		return null;
	}

}
