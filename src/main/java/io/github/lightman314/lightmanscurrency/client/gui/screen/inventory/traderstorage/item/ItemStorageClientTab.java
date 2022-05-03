package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.item.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.common.TraderItemStorage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ItemStorageClientTab extends TraderStorageClientTab<ItemStorageTab> implements IScrollListener, IScrollable{

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int COLUMNS = 8;
	private static final int ROWS = 5;
	
	public ItemStorageClientTab(TraderStorageScreen screen, ItemStorageTab commonTab) { super(screen, commonTab); }

	int scroll = 0;
	
	@Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_STORAGE; }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.trader.storage"); }

	@Override
	public boolean tabButtonVisible() { return true; }
	
	@Override
	public boolean blockInventoryClosing() { return false; }

	@Override
	public void onOpen() {
		
		this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + X_OFFSET + (18 * COLUMNS), this.screen.getGuiTop() + Y_OFFSET, ROWS * 18, this));
		
		
		this.screen.addTabListener(new ScrollListener(this.screen.getGuiLeft(), this.screen.getGuiTop(), this.screen.getXSize(), 118, this));
		
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.font.draw(pose, new TranslatableComponent("gui.lightmanscurrency.storage"), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040);
		
		if(this.menu.getTrader() instanceof IItemTrader)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each display slot
			int index = this.scroll * COLUMNS;
			TraderItemStorage storage = ((IItemTrader)this.menu.getTrader()).getStorage();
			int hoverSlot = this.isMouseOverSlot(mouseX, mouseY);
			for(int y = 0; y < ROWS; ++y)
			{
				int yPos = this.screen.getGuiTop() + Y_OFFSET + y * 18;
				for(int x = 0; x < COLUMNS; ++x)
				{
					//Get the slot position
					int xPos = this.screen.getGuiLeft() + X_OFFSET + x * 18;
					//Render the slot background
					RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
					RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
					this.screen.blit(pose, xPos, yPos, TraderScreen.WIDTH, 0, 18, 18);
					if(index == hoverSlot)
						AbstractContainerScreen.renderSlotHighlight(pose, xPos + 1, yPos + 1, this.screen.getBlitOffset());
					//Render the slots item
					if(index < storage.getSlotCount())
						ItemRenderUtil.drawItemStack(this.screen, this.font, storage.getContents().get(index), xPos + 1, yPos + 1, this.getCountText(storage.getContents().get(index)));
					index++;
				}
			}
			
			//Render the slot bg for the upgrade slots
			RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			for(Slot slot : this.commonTab.getSlots())
			{
				this.screen.blit(pose, this.screen.getGuiLeft() + slot.x - 1, this.screen.getGuiTop() + slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);
			}
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
				countText += "." + String.valueOf((count % 1000) / 100);
			return countText + "k";
		}
		return String.valueOf(count);
	}

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		if(this.menu.getTrader() instanceof IItemTrader && this.screen.getMenu().getCarried().isEmpty())
		{
			int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
			if(hoveredSlot >= 0)
			{
				hoveredSlot += scroll * COLUMNS;
				TraderItemStorage storage = ((IItemTrader)this.menu.getTrader()).getStorage();
				if(hoveredSlot < storage.getContents().size())
				{
					ItemStack stack = storage.getContents().get(hoveredSlot);
					List<Component> tooltip = ItemRenderUtil.getTooltipFromItem(stack);
					tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.itemstorage", stack.getCount()));
					if(stack.getCount() >= 64)
					{
						if(stack.getCount() % 64 == 0)
							tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.itemstorage.stacks.single", stack.getCount() / 64));
						else
							tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.itemstorage.stacks.multi", stack.getCount() / 64, stack.getCount() % 64));
					}
					this.screen.renderComponentTooltip(pose, tooltip, mouseX, mouseY);
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
		if(this.menu.getTrader() instanceof IItemTrader)
		{
			return ((IItemTrader)this.menu.getTrader()).getStorage().getContents().size();
		}
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
		
		if(this.menu.getTrader() instanceof IItemTrader)
		{
			int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
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
	
}
