package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.List;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

@IPNIgnore
public class ATMScreen extends ContainerScreen<ATMContainer> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/atm.png");
	
	public FontRenderer getFont() { return this.font; }
	
	int currentTabIndex = 0;
	List<ATMTab> tabs = Lists.newArrayList(new ConversionTab(this), new SelectionTab(this), new InteractionTab(this), new LogTab(this), new TransferTab(this));
	public List<ATMTab> getTabs() { return this.tabs; }
	public ATMTab currentTab() { return tabs.get(this.currentTabIndex); }
	
	List<Widget> tabWidgets = Lists.newArrayList();
	List<IGuiEventListener> tabListeners = Lists.newArrayList();
	
	List<TabButton> tabButtons = Lists.newArrayList();
	
	boolean logError = true;
	
	public ATMScreen(ATMContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.ySize = 212;
		this.xSize = 176;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(matrix, startX, startY, 0, 0, this.xSize, this.ySize);
		
		try {
			this.currentTab().backgroundRender(matrix);
		} catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } }
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		this.font.drawString(matrix, this.playerInventory.getDisplayName().getString(), 8.0f, (this.ySize - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		for(int i = 0; i < this.tabs.size(); ++i)
		{
			TabButton button = this.addButton(new TabButton(this::clickedOnTab, this.font, this.tabs.get(i)));
			button.reposition(this.guiLeft - TabButton.SIZE, this.guiTop + i * TabButton.SIZE, 3);
			button.active = i != this.currentTabIndex;
			this.tabButtons.add(button);
		}
		
		this.currentTab().init();
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		//Render the current tab
		try {
			this.currentTab().preRender(matrixStack, mouseX, mouseY, partialTicks);
			this.tabWidgets.forEach(widget -> widget.render(matrixStack, mouseX, mouseY, partialTicks));
			this.currentTab().postRender(matrixStack, mouseX, mouseY);
		} catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } }
		
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
		
		//Render the tab button tooltips
		for(int i = 0; i < this.tabButtons.size(); ++i)
		{
			if(this.tabButtons.get(i).isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrixStack, this.tabButtons.get(i).tab.getTooltip(), mouseX, mouseY);
		}
	}
	
	public void changeTab(int tabIndex)
	{
		int oldTab = this.currentTabIndex;
		//Close the old tab
		this.currentTab().onClose();
		this.tabButtons.get(this.currentTabIndex).active = true;
		this.currentTabIndex = tabIndex;
		this.tabButtons.get(this.currentTabIndex).active = false;
		
		LightmansCurrency.LogInfo("Changed from tab " + oldTab + " to tab " + this.currentTabIndex + ".");
		LightmansCurrency.LogInfo(this.tabWidgets.size() + " tab widgets & " + this.tabListeners.size() + " tab listeners were present.");
		
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
	
	public void tick()
	{
		this.currentTab().tick();
	}
	
	public <T extends Widget> T addRenderableTabWidget(T widget)
	{
		this.tabWidgets.add(widget);
		return widget;
	}
	
	public void removeRenderableTabWidget(Widget widget)
	{
		if(this.tabWidgets.contains(widget))
			this.tabWidgets.remove(widget);
	}
	
	public <T extends IGuiEventListener> T addTabListener(T listener)
	{
		this.tabListeners.add(listener);
		return listener;
	}
	
	public void removeTabListener(IGuiEventListener listener)
	{
		if(this.tabListeners.contains(listener))
			this.tabListeners.remove(listener);
	}
	
	@Override
	public List<? extends IGuiEventListener> getEventListeners()
	{
		List<? extends IGuiEventListener> coreListeners = super.getEventListeners();
		List<IGuiEventListener> listeners = Lists.newArrayList();
		for(int i = 0; i < coreListeners.size(); ++i)
			listeners.add(coreListeners.get(i));
		listeners.addAll(this.tabWidgets);
		listeners.addAll(this.tabListeners);
		return listeners;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
		if (this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey) && this.currentTab().blockInventoryClosing()) {
	         return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
}
