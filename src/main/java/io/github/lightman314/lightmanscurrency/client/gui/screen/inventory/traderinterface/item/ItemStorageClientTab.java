package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.item;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.item.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.trader.common.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.trader.settings.directional.DirectionalSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ItemStorageClientTab extends TraderInterfaceClientTab<ItemStorageTab> implements IScrollListener, IScrollable{

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int COLUMNS = 10;
	private static final int ROWS = 2;
	
	private static final int WIDGET_OFFSET = Y_OFFSET + 18 * ROWS + 4;
	
	DirectionalSettingsWidget inputSettings;
	DirectionalSettingsWidget outputSettings;
	
	public ItemStorageClientTab(TraderInterfaceScreen screen, ItemStorageTab commonTab) { super(screen, commonTab); }

	int scroll = 0;
	
	@Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_STORAGE; }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.interface.storage"); }
	
	@Override
	public boolean blockInventoryClosing() { return false; }
	
	private DirectionalSettings getInputSettings() {
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
			return ((ItemTraderInterfaceBlockEntity)this.menu.getBE()).getItemHandler().getInputSides();
		return new DirectionalSettings();
	}
	
	private DirectionalSettings getOutputSettings() { 
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
			return ((ItemTraderInterfaceBlockEntity)this.menu.getBE()).getItemHandler().getOutputSides();
		return new DirectionalSettings();
	}
	
	@Override
	public void onOpen() {
		
		this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + X_OFFSET + (18 * COLUMNS), this.screen.getGuiTop() + Y_OFFSET, ROWS * 18, this));
		
		this.screen.addTabListener(new ScrollListener(this.screen.getGuiLeft(), this.screen.getGuiTop(), this.screen.getXSize(), 118, this));
		
		this.inputSettings = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 33, this.screen.getGuiTop() + WIDGET_OFFSET + 9, this::getInputSettings, this::ToggleInputSide, this.screen::addRenderableTabWidget);
		this.outputSettings = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 116, this.screen.getGuiTop() + WIDGET_OFFSET + 9, this::getOutputSettings, this::ToggleOutputSide, this.screen::addRenderableTabWidget);
		
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.font.draw(pose, new TranslatableComponent("tooltip.lightmanscurrency.interface.storage"), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040);
		
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each display slot
			int index = this.scroll * COLUMNS;
			TraderItemStorage storage = ((ItemTraderInterfaceBlockEntity)this.menu.getBE()).getItemBuffer();
			int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY) + (this.scroll * COLUMNS);
			for(int y = 0; y < ROWS; ++y)
			{
				int yPos = this.screen.getGuiTop() + Y_OFFSET + y * 18;
				for(int x = 0; x < COLUMNS; ++x)
				{
					//Get the slot position
					int xPos = this.screen.getGuiLeft() + X_OFFSET + x * 18;
					//Render the slot background
					RenderSystem.setShaderTexture(0, TraderInterfaceScreen.GUI_TEXTURE);
					RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
					this.screen.blit(pose, xPos, yPos, TraderInterfaceScreen.WIDTH, 0, 18, 18);
					//Render the slots item
					if(index < storage.getSlotCount())
						ItemRenderUtil.drawItemStack(this.screen, this.font, storage.getContents().get(index), xPos + 1, yPos + 1, this.getCountText(storage.getContents().get(index)));
					if(index == hoveredSlot)
						AbstractContainerScreen.renderSlotHighlight(pose, xPos + 1, yPos + 1, this.screen.getBlitOffset());
					index++;
				}
			}
			
			//Render the slot bg for the upgrade slots
			RenderSystem.setShaderTexture(0, TraderInterfaceScreen.GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			for(Slot slot : this.commonTab.getSlots())
			{
				this.screen.blit(pose, this.screen.getGuiLeft() + slot.x - 1, this.screen.getGuiTop() + slot.y - 1, TraderInterfaceScreen.WIDTH, 0, 18, 18);
			}
			
			//Render the input/output labels
			this.font.draw(pose, new TranslatableComponent("gui.lightmanscurrency.settings.iteminput.side"), this.screen.getGuiLeft() + 33, this.screen.getGuiTop() + WIDGET_OFFSET, 0x404040);
			this.font.draw(pose, new TranslatableComponent("gui.lightmanscurrency.settings.itemoutput.side"), this.screen.getGuiLeft() + 116, this.screen.getGuiTop() + WIDGET_OFFSET, 0x404040);
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
		
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
		{
			if(this.screen.getMenu().getCarried().isEmpty())
			{
				int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
				if(hoveredSlot >= 0)
				{
					hoveredSlot += scroll * COLUMNS;
					TraderItemStorage storage = ((ItemTraderInterfaceBlockEntity)this.menu.getBE()).getItemBuffer();
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
			
			this.inputSettings.renderTooltips(pose, mouseX, mouseY, this.screen);
			this.outputSettings.renderTooltips(pose, mouseX, mouseY, this.screen);
			
		}
	}
	
	@Override
	public void tick() {
		this.inputSettings.tick();
		this.outputSettings.tick();
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
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
		{
			return ((ItemTraderInterfaceBlockEntity)this.menu.getBE()).getItemBuffer().getContents().size();
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
		
		if(this.menu.getBE() instanceof ItemTraderInterfaceBlockEntity)
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
	
	private void ToggleInputSide(Direction side) {
		this.commonTab.toggleInputSlot(side);
	}
	
	private void ToggleOutputSide(Direction side) {
		this.commonTab.toggleOutputSlot(side);
	}
	
}
