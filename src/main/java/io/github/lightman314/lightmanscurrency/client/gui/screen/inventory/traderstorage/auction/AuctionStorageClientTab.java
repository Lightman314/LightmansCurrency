package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction;

import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction.AuctionStorageTab;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

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
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_STORAGE; }
	
	@Override
	public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.auction.storage"); }
	
	@Override
	public boolean blockInventoryClosing() { return false; }
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.scrollBar = this.addChild(new ScrollBarWidget(screenArea.pos.offset(X_OFFSET + (18 * COLUMNS), Y_OFFSET), ROWS * 18, this));
		
		this.buttonCollectItems = this.addChild(IconAndButtonUtil.quickExtractButton(screenArea.pos.offset(11, Y_OFFSET + 18 * ROWS + 8), b -> this.commonTab.quickTransfer()));
		
		this.buttonCollectMoney = this.addChild(new IconButton(screenArea.pos.offset(25, 118), b -> this.commonTab.collectCoins(), IconAndButtonUtil.ICON_COLLECT_COINS));
		
		this.addChild(new ScrollListener(screenArea.pos, screenArea.width, 118, this));
		
	}
	
	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		gui.drawString(EasyText.translatable("tooltip.lightmanscurrency.auction.storage"), 8, 6, 0x404040);
		
		if(this.menu.getTrader() instanceof AuctionHouseTrader trader)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each display slot
			int index = this.scroll * COLUMNS;
			AuctionPlayerStorage storage = trader.getStorage(this.menu.player);
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
						gui.blit( TraderScreen.GUI_TEXTURE, xPos, yPos, TraderScreen.WIDTH, 0, 18, 18);
						//Render the slots item
						if(index < storedItems.size())
							gui.renderItem(storedItems.get(index), xPos + 1, yPos + 1);
						if(index == hoverSlot)
							gui.renderSlotHighlight(xPos + 1, yPos + 1);
						index++;
					}
				}
				
				if(storedItems.size() == 0)
					TextRenderUtil.drawCenteredMultilineText(gui, EasyText.translatable("tooltip.lightmanscurrency.auction.storage.items.none"), 10, this.screen.getXSize() - 20, X_OFFSET + (18 * ROWS / 2), 0x404040);
				
				this.buttonCollectItems.active = storedItems.size() > 0;
				
				//Render the stored money amount
				if(storage.getStoredCoins().hasAny())
				{
					this.buttonCollectMoney.active = true;
					gui.drawString(EasyText.translatable("tooltip.lightmanscurrency.auction.storage.money", storage.getStoredCoins().getString("0")), 50, 118, 0x404040);
				}
				else
				{
					this.buttonCollectMoney.active = false;
					gui.drawString(EasyText.translatable("tooltip.lightmanscurrency.auction.storage.money.none"), 50, 118, 0x404040);
				}
				
			}
			
		}
		
	}
	
	@Override
	public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		
		if(this.menu.getTrader() instanceof AuctionHouseTrader && this.screen.getMenu().getCarried().isEmpty())
		{
			int hoveredSlot = this.isMouseOverSlot(gui.mousePos);
			if(hoveredSlot >= 0)
			{
				hoveredSlot += scroll * COLUMNS;
				AuctionPlayerStorage storage = ((AuctionHouseTrader)this.menu.getTrader()).getStorage(this.menu.player);
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
		if(this.menu.getTrader() instanceof AuctionHouseTrader trader)
			return trader.getStorage(this.menu.player).getStoredItems().size();
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
	
}
