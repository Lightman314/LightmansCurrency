package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.itemInterface.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.menus.ItemInterfaceMenu;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.MessageToggleInteractionActive;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeComparisonResult.ProductComparisonResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class ItemInterfaceScreen extends AbstractContainerScreen<ItemInterfaceMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/item_interface.png");
	
	int currentTabIndex = 0;
	List<ItemInterfaceTab> tabs = Lists.newArrayList(new InfoTab(this));
	public List<ItemInterfaceTab> getTabs() { return this.tabs; }
	public ItemInterfaceTab currentTab() { return tabs.get(this.currentTabIndex); }
	
	List<AbstractWidget> tabWidgets = Lists.newArrayList();
	List<GuiEventListener> tabListeners = Lists.newArrayList();
	
	List<TabButton> tabButtons = Lists.newArrayList();
	
	IconButton buttonActivate;
	
	boolean logError = true;
	
	public ItemInterfaceScreen(ItemInterfaceMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = 212;
		this.imageWidth = 176;
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		this.font.draw(poseStack, this.playerInventoryTitle, 8.0f, (this.imageHeight - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		for(int i = 0; i < this.tabs.size(); ++i)
		{
			TabButton button = this.addRenderableWidget(new TabButton(this::clickedOnTab, this.font, this.tabs.get(i)));
			button.reposition(this.leftPos - TabButton.SIZE, this.topPos + i * TabButton.SIZE, 3);
			button.active = i != this.currentTabIndex;
			this.tabButtons.add(button);
		}
		
		this.buttonActivate = this.addRenderableWidget(IconAndButtonUtil.interfaceActiveToggleButton(this.leftPos + this.imageWidth, this.topPos, this::ToggleActive, this.menu.blockEntity::interactionActive));
		
		this.currentTab().init();
		
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(pose);
		
		//Render the tab buttons & background
		super.render(pose, mouseX, mouseY, partialTicks);
		
		//Render the current tab
		try {
			this.currentTab().preRender(pose, mouseX, mouseY, partialTicks);
			this.tabWidgets.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
			this.currentTab().postRender(pose, mouseX, mouseY);
		} catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } } 
		
		this.renderTooltip(pose, mouseX,  mouseY);
		
		//Render the tab button tooltips
		for(int i = 0; i < this.tabButtons.size(); ++i)
		{
			if(this.tabButtons.get(i).isMouseOver(mouseX, mouseY))
				this.renderTooltip(pose, this.tabButtons.get(i).tab.getTooltip(), mouseX, mouseY);
		}
		
	}

	public void changeTab(int tabIndex)
	{
		
		//Close the old tab
		this.currentTab().onClose();
		this.tabButtons.get(this.currentTabIndex).active = true;
		this.currentTabIndex = tabIndex;
		this.tabButtons.get(this.currentTabIndex).active = false;
		
		//Clear the previous tabs widgets
		this.tabWidgets.clear();
		this.tabListeners.clear();
		
		//Initialize the new tab
		this.currentTab().init();
		
		this.logError = true;
	}
	
	private void clickedOnTab(Button tab)
	{
		int tabIndex = this.tabButtons.indexOf(tab);
		if(tabIndex < 0)
			return;
		this.changeTab(tabIndex);
	}
	
	public void containerTick()
	{
		this.buttonActivate.active = this.validState();
		this.currentTab().tick();
	}
	
	public <T extends AbstractWidget> T addRenderableTabWidget(T widget)
	{
		this.tabWidgets.add(widget);
		return widget;
	}
	
	public void removeRenderableTabWidget(AbstractWidget widget)
	{
		if(this.tabWidgets.contains(widget))
			this.tabWidgets.remove(widget);
	}
	
	public <T extends GuiEventListener> T addTabListener(T listener)
	{
		this.tabListeners.add(listener);
		return listener;
	}
	
	public void removeTabListener(GuiEventListener listener)
	{
		if(this.tabListeners.contains(listener))
			this.tabListeners.remove(listener);
	}
	
	public Font getFont() {
		return this.font;
	}
	
	@Override
	public List<? extends GuiEventListener> children()
	{
		List<? extends GuiEventListener> coreListeners = super.children();
		List<GuiEventListener> listeners = Lists.newArrayList();
		for(int i = 0; i < coreListeners.size(); ++i)
			listeners.add(coreListeners.get(i));
		listeners.addAll(this.tabWidgets);
		listeners.addAll(this.tabListeners);
		return listeners;
	}
	
	@Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
	      InputConstants.Key mouseKey = InputConstants.getKey(p_97765_, p_97766_);
	      //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
	      if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.currentTab().blockInventoryClosing()) {
	    	  return true;
	      }
	      return super.keyPressed(p_97765_, p_97766_, p_97767_);
	}
	
	private void ToggleActive(Button button) {
		this.menu.blockEntity.toggleActive();
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageToggleInteractionActive(this.menu.blockEntity.getBlockPos(), this.menu.blockEntity.interactionActive()));
	}
	
	public ItemTradeData getReferencedTrade() {
		ItemTradeData trade = this.menu.blockEntity.getReferencedTrade();
		if(trade == null)
			trade = new ItemTradeData();
		return trade;
	}
	
	public ItemTradeData getTrueTrade() {
		ItemTradeData trade = this.menu.blockEntity.getTrueTrade();
		if(trade == null)
			trade = new ItemTradeData();
		return trade;
	}
	
	public boolean validState() {
		if(this.menu.blockEntity.getInteractionType().requiresPermissions)
			return this.menu.blockEntity.validTrader();
		else if(this.menu.blockEntity.getInteractionType().trades)
			return this.menu.blockEntity.validTrade();
		return false;
	}
	
	public boolean changeInTrades() {
		ItemTradeData referencedTrade = this.menu.blockEntity.getReferencedTrade();
		ItemTradeData trueTrade = this.menu.blockEntity.getTrueTrade();
		if(referencedTrade == null)
			return false;
		if(trueTrade == null)
			return true;
		return !referencedTrade.compare(trueTrade).Identical();
	}
	
	public List<Component> getTradeChangeMessage() {
		List<Component> list = new ArrayList<>();
		ItemTradeData referencedTrade = this.menu.blockEntity.getReferencedTrade();
		ItemTradeData trueTrade = this.menu.blockEntity.getTrueTrade();
		if(referencedTrade == null)
			return list;
		if(trueTrade == null)
		{
			list.add(new TranslatableComponent("gui.lightmanscurrency.interface.item.difference.missing"));
			return list;
		}
		TradeComparisonResult differences = referencedTrade.compare(trueTrade);
		//Type check
		if(!differences.TypeMatches())
		{
			list.add(new TranslatableComponent("gui.lightmanscurrency.interface.item.difference.type"));
			return list;
		}
		//Price check
		if(!differences.PriceMatches())
		{
			//Price difference (intended - actual = difference)
			long difference = differences.priceDifference();
			if(difference < 0) //More expensive
				list.add(new TranslatableComponent("gui.lightmanscurrency.interface.item.difference.expensive", MoneyUtil.getStringOfValue(-difference)));
			else //Cheaper
				list.add(new TranslatableComponent("gui.lightmanscurrency.interface.item.difference.cheaper", MoneyUtil.getStringOfValue(difference)));
		}
		for(int i = 0; i < differences.getProductResultCount(); ++i)
		{
			Component slotName = new TranslatableComponent("gui.lightmanscurrency.interface.item.difference.product." + i);
			ProductComparisonResult productCheck = differences.getProductResult(i);
			if(!productCheck.SameProductType())
				list.add(new TranslatableComponent("gui.lightmanscurrency.interface.item.difference.itemtype", slotName));
			else
			{
				if(!productCheck.SameProductNBT()) //Don't announce changes in NBT if the item is also different
					list.add(new TranslatableComponent("gui.lightmanscurrency.interface.item.difference.itemnbt"));
				else if(!productCheck.SameProductQuantity()) //Don't announce changes in quantity if the item or nbt is also different
				{
					int quantityDifference = productCheck.ProductQuantityDifference();
					if(quantityDifference < 0) //More items
						list.add(new TranslatableComponent("gui.lightmanscurrency.interface.item.difference.quantity.more", slotName, -quantityDifference));
					else //Less items
						list.add(new TranslatableComponent("gui.lightmanscurrency.interface.item.difference.quantity.less", slotName, quantityDifference));	
				}
			}
		}
		return list;
	}
	
}
