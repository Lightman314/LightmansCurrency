package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class ATMScreen extends AbstractContainerScreen<ATMMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/atm.png");
	
	public static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/atm_buttons.png");
	
	int currentTabIndex = 0;
	List<ATMTab> tabs = Lists.newArrayList(new ConversionTab(this), new SelectionTab(this), new InteractionTab(this), new NotificationTab(this), new LogTab(this), new TransferTab(this));
	public List<ATMTab> getTabs() { return this.tabs; }
	public ATMTab currentTab() { return tabs.get(this.currentTabIndex); }
	
	List<AbstractWidget> tabWidgets = new ArrayList<>();
	List<GuiEventListener> tabListeners = new ArrayList<>();
	
	List<TabButton> tabButtons = new ArrayList<>();
	
	boolean logError = true;
	
	public ATMScreen(ATMMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = 243;
		this.imageWidth = 176;
	}
	
	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		try {
			this.currentTab().preRender(pose, mouseX, mouseY, partialTicks);
			this.tabWidgets.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
		} catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } } 
		
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
		
		this.tabWidgets.clear();
		this.tabListeners.clear();
		
		this.tabButtons = new ArrayList<>();
		for(int i = 0; i < this.tabs.size(); ++i)
		{
			TabButton button = this.addRenderableWidget(new TabButton(this::clickedOnTab, this.font, this.tabs.get(i)));
			button.reposition(this.leftPos - TabButton.SIZE, this.topPos + i * TabButton.SIZE, 3);
			button.active = i != this.currentTabIndex;
			this.tabButtons.add(button);
		}
		
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
			//this.currentTab().preRender(pose, mouseX, mouseY, partialTicks);
			//this.tabWidgets.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
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
		this.currentTabIndex = MathUtil.clamp(tabIndex, 0, this.tabs.size() - 1);
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
	
}
