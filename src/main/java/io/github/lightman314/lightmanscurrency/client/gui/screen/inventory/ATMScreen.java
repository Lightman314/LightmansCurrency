package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class ATMScreen extends ContainerScreen<ATMMenu> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/atm.png");
	
	public static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/atm_buttons.png");
	
	int currentTabIndex = 0;
	List<ATMTab> tabs = Lists.newArrayList(new ConversionTab(this), new SelectionTab(this), new InteractionTab(this), new NotificationTab(this), new LogTab(this), new TransferTab(this));
	public List<ATMTab> getTabs() { return this.tabs; }
	public ATMTab currentTab() { return tabs.get(this.currentTabIndex); }
	
	List<IRenderable> tabWidgets = new ArrayList<>();
	List<IGuiEventListener> tabListeners = new ArrayList<>();
	
	List<TabButton> tabButtons = new ArrayList<>();
	
	boolean logError = true;
	
	public ATMScreen(ATMMenu container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.imageHeight = 243;
		this.imageWidth = 176;
	}
	
	@Override
	protected void renderBg(@Nonnull MatrixStack pose, float partialTicks, int mouseX, int mouseY)
	{

		RenderUtil.bindTexture(GUI_TEXTURE);
		RenderUtil.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		try {
			this.currentTab().preRender(pose, mouseX, mouseY, partialTicks);
			this.tabWidgets.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
		} catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } } 
		
	}
	
	@Override
	protected void renderLabels(@Nonnull MatrixStack poseStack, int mouseX, int mouseY)
	{
		this.font.draw(poseStack, this.inventory.getName(), 8.0f, (this.imageHeight - 94), 0x404040);
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
			TabButton button = this.addButton(new TabButton(this::clickedOnTab, this.font, this.tabs.get(i)));
			button.reposition(this.leftPos - TabButton.SIZE, this.topPos + i * TabButton.SIZE, 3);
			button.active = i != this.currentTabIndex;
			this.tabButtons.add(button);
		}
		
		this.currentTab().init();
		
	}
	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(pose);
		
		//Render the tab buttons & background
		super.render(pose, mouseX, mouseY, partialTicks);
		
		//Render the current tab
		try {
			this.currentTab().postRender(pose, mouseX, mouseY);
		} catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } } 
		
		this.renderTooltip(pose, mouseX,  mouseY);
		
		//Render the tab button tooltips
		for (TabButton tabButton : this.tabButtons) {
			if (tabButton.isMouseOver(mouseX, mouseY))
				this.renderTooltip(pose, tabButton.tab.getTooltip(), mouseX, mouseY);
		}

		ITooltipSource.renderTooltips(this, pose, mouseX, mouseY);
		
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

	@Override
	public void tick()
	{
		this.currentTab().tick();
	}
	
	public <T extends IRenderable> T addRenderableTabWidget(T widget)
	{
		this.tabWidgets.add(widget);
		if(widget instanceof IGuiEventListener)
			this.addTabListener((IGuiEventListener)widget);
		return widget;
	}
	
	public void removeRenderableTabWidget(IRenderable widget)
	{
		this.tabWidgets.remove(widget);
		if(widget instanceof IGuiEventListener)
			this.removeTabListener((IGuiEventListener)widget);
	}
	
	public <T extends IGuiEventListener> T addTabListener(T listener)
	{
		this.tabListeners.add(listener);
		return listener;
	}
	
	public void removeTabListener(IGuiEventListener listener)
	{
		this.tabListeners.remove(listener);
	}
	
	public FontRenderer getFont() {
		return this.font;
	}
	
	@Override
	public List<? extends IGuiEventListener> children()
	{
		List<? extends IGuiEventListener> coreListeners = super.children();
		List<IGuiEventListener> listeners = Lists.newArrayList();
		listeners.addAll(coreListeners);
		listeners.addAll(this.tabListeners);
		return listeners;
	}
	
	@Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
		InputMappings.Input mouseKey = InputMappings.getKey(p_97765_, p_97766_);
		//Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.currentTab().blockInventoryClosing()) {
			return true;
		}
		return super.keyPressed(p_97765_, p_97766_, p_97767_);
	}
	
}
