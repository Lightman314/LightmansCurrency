package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.menus.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageOpenWallet;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

@IPNIgnore
public class WalletBankScreen extends AbstractContainerScreen<WalletBankMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet_bank.png");
	
	int currentTabIndex = 0;
	List<WalletBankTab> tabs = Lists.newArrayList(new InteractionTab(this), new SelectionTab(this));
	public List<WalletBankTab> getTabs() { return this.tabs; }
	public WalletBankTab currentTab() { return tabs.get(this.currentTabIndex); }
	
	List<AbstractWidget> tabWidgets = new ArrayList<>();
	List<GuiEventListener> tabListeners = new ArrayList<>();
	
	List<TabButton> tabButtons = new ArrayList<>();
	
	boolean logError = true;
	
	Button buttonOpenWallet;
	
	public WalletBankScreen(WalletBankMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected void init()
	{
		
		this.imageHeight = WalletBankMenu.BANK_WIDGET_SPACING + this.menu.getRowCount() * 18 + 7;
		this.imageWidth = 176;
		
		super.init();
		
		this.clearWidgets();
		
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
		
		this.buttonOpenWallet = this.addRenderableWidget(new IconButton(this.leftPos, this.topPos - 20, this::PressOpenWalletButton, IconData.of(this.menu.getWallet())));
		
		this.currentTab().init();
		
	}
	
	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		//Draw the top
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, WalletBankMenu.BANK_WIDGET_SPACING);
		//Draw the middle strips
		for(int y = 0; y < this.menu.getRowCount(); y++)
			this.blit(pose, this.leftPos, this.topPos + WalletBankMenu.BANK_WIDGET_SPACING + y * 18, 0, WalletBankMenu.BANK_WIDGET_SPACING, this.imageWidth, 18);
		
		//Draw the bottom
		this.blit(pose, this.leftPos, this.topPos + WalletBankMenu.BANK_WIDGET_SPACING + this.menu.getRowCount() * 18, 0, WalletBankMenu.BANK_WIDGET_SPACING + 18, this.imageWidth, 7);
		
		//Draw the slots
		for(int y = 0; y * 9 < this.menu.getSlotCount(); y++)
		{
			for(int x = 0; x < 9 && x + y * 9 < this.menu.getSlotCount(); x++)
			{
				this.blit(pose, this.leftPos + 7 + x * 18, this.topPos + WalletBankMenu.BANK_WIDGET_SPACING + y * 18, 0, WalletBankMenu.BANK_WIDGET_SPACING + 18 + 7, 18, 18);
			}
		}
		
		//Render Current Tab
		try {
			this.currentTab().preRender(pose, mouseX, mouseY, partialTicks);
			this.tabWidgets.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
		} catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } } 
		
	}
	
	private Component getWalletName() {
		ItemStack wallet = this.menu.getWallet();
		return wallet.isEmpty() ? Component.empty() : wallet.getHoverName();
	}
	
	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
		
		this.font.draw(pose, this.getWalletName(), 8.0f, WalletBankMenu.BANK_WIDGET_SPACING - 11, 0x404040);
		
	}

	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		
		//Render the current tab
		try {
			this.currentTab().postRender(pose, mouseX, mouseY);
		} catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } } 
		
		this.renderTooltip(pose, mouseX,  mouseY);
		
		if(this.buttonOpenWallet != null && this.buttonOpenWallet.isMouseOver(mouseX, mouseY))
			this.renderTooltip(pose, Component.translatable("tooltip.lightmanscurrency.wallet.openwallet"), mouseX, mouseY);
		
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
	
	private void PressOpenWalletButton(Button button) {
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet(this.menu.getWalletStackIndex()));
	}
	
	
}
