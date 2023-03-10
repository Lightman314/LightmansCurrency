package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageOpenWallet;
import io.github.lightman314.lightmanscurrency.util.MathUtil;

import javax.annotation.Nonnull;

@IPNIgnore
public class WalletBankScreen extends ContainerScreen<WalletBankMenu> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet_bank.png");
	
	int currentTabIndex = 0;
	List<WalletBankTab> tabs = Lists.newArrayList(new InteractionTab(this), new SelectionTab(this));
	public List<WalletBankTab> getTabs() { return this.tabs; }
	public WalletBankTab currentTab() { return tabs.get(this.currentTabIndex); }
	
	List<Widget> tabWidgets = new ArrayList<>();
	List<IGuiEventListener> tabListeners = new ArrayList<>();
	
	List<TabButton> tabButtons = new ArrayList<>();
	
	boolean logError = true;
	
	Button buttonOpenWallet;
	
	public WalletBankScreen(WalletBankMenu menu, PlayerInventory inventory, ITextComponent title) {
		super(menu, inventory, title);
	}

	@Override
	protected void init()
	{
		
		this.imageHeight = WalletBankMenu.BANK_WIDGET_SPACING + this.menu.getRowCount() * 18 + 7;
		this.imageWidth = 176;
		
		super.init();
		
		this.buttons.clear();
		this.children.clear();
		
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
		
		this.buttonOpenWallet = this.addButton(new IconButton(this.leftPos, this.topPos - 20, this::PressOpenWalletButton, IconData.of(this.menu.getWallet())));
		
		this.currentTab().init();
		
	}
	
	@Override
	protected void renderBg(@Nonnull MatrixStack pose, float partialTicks, int mouseX, int mouseY) {
		
		RenderUtil.bindTexture(GUI_TEXTURE);
		RenderUtil.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
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
	
	private ITextComponent getWalletName() {
		ItemStack wallet = this.menu.getWallet();
		return wallet.isEmpty() ? EasyText.empty() : wallet.getHoverName();
	}
	
	@Override
	protected void renderLabels(@Nonnull MatrixStack pose, int mouseX, int mouseY) {
		
		this.font.draw(pose, this.getWalletName(), 8.0f, WalletBankMenu.BANK_WIDGET_SPACING - 11, 0x404040);
		
	}

	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		
		//Render the current tab
		try {
			this.currentTab().postRender(pose, mouseX, mouseY);
		} catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } } 
		
		this.renderTooltip(pose, mouseX,  mouseY);
		
		if(this.buttonOpenWallet != null && this.buttonOpenWallet.isMouseOver(mouseX, mouseY))
			this.renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.wallet.openwallet"), mouseX, mouseY);
		
		//Render the tab button tooltips
		for (TabButton tabButton : this.tabButtons) {
			if (tabButton.isMouseOver(mouseX, mouseY))
				this.renderTooltip(pose, tabButton.tab.getTooltip(), mouseX, mouseY);
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

	@Override
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
		this.tabWidgets.remove(widget);
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
	
	@Nonnull
	@Override
	public List<? extends IGuiEventListener> children()
	{
		List<? extends IGuiEventListener> coreListeners = super.children();
		List<IGuiEventListener> listeners = Lists.newArrayList();
		listeners.addAll(coreListeners);
		listeners.addAll(this.tabWidgets);
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
	
	private void PressOpenWalletButton(Button button) {
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet(this.menu.getWalletStackIndex()));
	}
	
	
}
