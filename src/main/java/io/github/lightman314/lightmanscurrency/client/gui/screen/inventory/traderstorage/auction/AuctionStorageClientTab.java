package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction.AuctionStorageTab;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class AuctionStorageClientTab extends TraderStorageClientTab<AuctionStorageTab> implements IScrollListener, IScrollable {

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int COLUMNS = 10;
	private static final int ROWS = 4;
	
	public AuctionStorageClientTab(TraderStorageScreen screen, AuctionStorageTab commonTab) { super(screen, commonTab); }
	
	int scroll = 0;
	
	ScrollBarWidget scrollBar;
	
	Button buttonCollectItems;
	
	IconButton buttonCollectMoney;
	
	@Override
	public @NotNull IconData getIcon() { return IconAndButtonUtil.ICON_STORAGE; }
	
	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.auction.storage"); }
	
	@Override
	public boolean tabButtonVisible() { return true; }
	
	@Override
	public boolean blockInventoryClosing() { return false; }
	
	@Override
	public void onOpen() {
		
		this.scrollBar = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + X_OFFSET + (18 * COLUMNS), this.screen.getGuiTop() + Y_OFFSET, ROWS * 18, this));
		
		this.buttonCollectItems = this.screen.addRenderableTabWidget(IconAndButtonUtil.quickExtractButton(this.screen.getGuiLeft() + 11, this.screen.getGuiTop() + Y_OFFSET + 18 * ROWS + 8, b -> this.commonTab.quickTransfer()));
		
		this.buttonCollectMoney = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + 25, this.screen.getGuiTop() + 118, b -> this.commonTab.collectCoins(), IconAndButtonUtil.ICON_COLLECT_COINS));
		
		this.screen.addTabListener(new ScrollListener(this.screen.getGuiLeft(), this.screen.getGuiTop(), this.screen.getXSize(), 118, this));
		
	}
	
	@Override
	public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.font.draw(pose, Component.translatable("tooltip.lightmanscurrency.auction.storage"), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040);
		
		this.scrollBar.beforeWidgetRender(mouseY);
		
		if(this.menu.getTrader() instanceof AuctionHouseTrader)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each display slot
			int index = this.scroll * COLUMNS;
			AuctionPlayerStorage storage = ((AuctionHouseTrader)this.menu.getTrader()).getStorage(this.menu.player);
			if(storage != null)
			{
				List<ItemStack> storedItems = storage.getStoredItems();
				int hoverSlot = this.isMouseOverSlot(mouseX, mouseY) + (this.scroll * COLUMNS);
				for(int y = 0; y < ROWS && index < storedItems.size(); ++y)
				{
					int yPos = this.screen.getGuiTop() + Y_OFFSET + y * 18;
					for(int x = 0; x < COLUMNS && index < storedItems.size(); ++x)
					{
						//Get the slot position
						int xPos = this.screen.getGuiLeft() + X_OFFSET + x * 18;
						//Render the slot background
						RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
						RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
						GuiComponent.blit(pose, xPos, yPos, TraderScreen.WIDTH, 0, 18, 18);
						//Render the slots item
						if(index < storedItems.size())
							ItemRenderUtil.drawItemStack(pose, this.font, storedItems.get(index), xPos + 1, yPos + 1);
						if(index == hoverSlot)
							AbstractContainerScreen.renderSlotHighlight(pose, xPos + 1, yPos + 1, 0);
						index++;
					}
				}
				
				if(storedItems.size() == 0)
					TextRenderUtil.drawCenteredMultilineText(pose, Component.translatable("tooltip.lightmanscurrency.auction.storage.items.none"), this.screen.getGuiLeft() + 10, this.screen.getXSize() - 20, this.screen.getGuiTop() + X_OFFSET + (18 * ROWS / 2), 0x404040);
				
				this.buttonCollectItems.active = storedItems.size() > 0;
				
				//Render the stored money amount
				if(storage.getStoredCoins().hasAny())
				{
					this.buttonCollectMoney.active = true;
					this.font.draw(pose, Component.translatable("tooltip.lightmanscurrency.auction.storage.money", storage.getStoredCoins().getString("0")), this.screen.getGuiLeft() + 50, this.screen.getGuiTop() + 118, 0x404040);
				}
				else
				{
					this.buttonCollectMoney.active = false;
					this.font.draw(pose, Component.translatable("tooltip.lightmanscurrency.auction.storage.money.none"), this.screen.getGuiLeft() + 50, this.screen.getGuiTop() + 118, 0x404040);
				}
				
			}
			
		}
		
	}
	
	@Override
	public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {
		
		if(this.menu.getTrader() instanceof AuctionHouseTrader && this.screen.getMenu().getCarried().isEmpty())
		{
			int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
			if(hoveredSlot >= 0)
			{
				hoveredSlot += scroll * COLUMNS;
				AuctionPlayerStorage storage = ((AuctionHouseTrader)this.menu.getTrader()).getStorage(this.menu.player);
				if(hoveredSlot < storage.getStoredItems().size())
				{
					ItemStack stack = storage.getStoredItems().get(hoveredSlot);
					this.screen.renderComponentTooltip(pose, ItemRenderUtil.getTooltipFromItem(stack), mouseX, mouseY);
				}	
			}
		}
	}
	
	private void validateScroll() {
		if(this.scroll < 0)
			this.scroll = 0;
		if(this.scroll > this.getMaxScroll())
			this.scroll = this.getMaxScroll();
	}
	
	private int isMouseOverSlot(double mouseX, double mouseY) {
		
		int foundColumn = -1;
		int foundRow = -1;
		
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;
		for(int x = 0; x < COLUMNS && foundColumn < 0; ++x)
		{
			if(mouseX >= leftEdge + x * 18 && mouseX < leftEdge + (x * 18) + 18)
				foundColumn = x;
		}
		for(int y = 0; y < ROWS && foundRow < 0; ++y)
		{
			if(mouseY >= topEdge + y * 18 && mouseY < topEdge + (y * 18) + 18)
				foundRow = y;
		}
		if(foundColumn < 0 || foundRow < 0)
			return -1;
		return (foundRow * COLUMNS) + foundColumn;
		
	}
	
	private int totalStorageSlots() {
		if(this.menu.getTrader() instanceof AuctionHouseTrader)
			return ((AuctionHouseTrader)this.menu.getTrader()).getStorage(this.menu.player).getStoredItems().size();
		return 0;
	}
	
	private boolean canScrollDown() {
		return this.totalStorageSlots() - this.scroll * COLUMNS > ROWS * COLUMNS;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if(delta < 0)
		{			
			if(this.canScrollDown())
				this.scroll++;
			else
				return false;
		}
		else if(delta > 0)
		{
			if(this.scroll > 0)
				scroll--;
			else
				return false;
		}
		return true;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		
		if(this.menu.getTrader() instanceof AuctionHouseTrader)
		{
			int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
			if(hoveredSlot >= 0)
			{
				hoveredSlot += this.scroll * COLUMNS;
				this.commonTab.clickedOnSlot(hoveredSlot, Screen.hasShiftDown());
				return true;
			}
		}
		this.scrollBar.onMouseClicked(mouseX, mouseY, button);
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.scrollBar.onMouseReleased(mouseX, mouseY, button);
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
