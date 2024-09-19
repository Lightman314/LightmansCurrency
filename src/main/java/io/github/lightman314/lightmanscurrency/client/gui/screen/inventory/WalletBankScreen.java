package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketOpenWallet;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletBankScreen extends EasyMenuScreen<WalletBankMenu> {

	public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "textures/gui/container/wallet_bank.png");
	
	int currentTabIndex = 0;
	List<WalletBankTab> tabs = Lists.newArrayList(new InteractionTab(this), new SelectionTab(this));
	public int getTabCount() { return this.tabs.size(); }
	public List<WalletBankTab> getTabs() { return this.tabs; }
	public WalletBankTab currentTab() { return tabs.get(this.currentTabIndex); }
	
	List<TabButton> tabButtons = new ArrayList<>();
	
	boolean logError = true;
	
	EasyButton buttonOpenWallet;
	
	public WalletBankScreen(WalletBankMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected void initialize(ScreenArea screenArea)
	{

		screenArea = this.resize(176 + this.menu.bonusWidth, WalletBankMenu.BANK_WIDGET_SPACING + this.menu.coinSlotHeight * 18 + 7);
		
		this.tabButtons = new ArrayList<>();
		for(int i = 0; i < this.tabs.size(); ++i)
		{
			TabButton button = this.addChild(new TabButton(this::clickedOnTab, this.tabs.get(i)));
			button.reposition(this.leftPos - TabButton.SIZE, this.topPos + i * TabButton.SIZE, 3);
			button.active = i != this.currentTabIndex;
			this.tabButtons.add(button);
		}
		
		this.buttonOpenWallet = this.addChild(new IconButton(screenArea.pos.offset(screenArea.width, 0), this::PressOpenWalletButton, IconData.of(this.menu.getWallet()))
				.withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_WALLET_OPEN_WALLET)));

		this.currentTab().onOpen();
		
	}
	
	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui) {

		gui.resetColor();
		//Draw the top
		gui.renderNormalBackground(this);

		//Draw the coin slots
		for(Slot slot : this.menu.slots)
			gui.renderSlot(this,slot);
		
		//Render Current Tab
		try { this.currentTab().renderBG(gui);
		} catch(Throwable e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } }

		gui.drawString(this.getWalletName(), 8, WalletBankMenu.BANK_WIDGET_SPACING - 11, 0x404040);

	}

	@Override
	protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		try { this.currentTab().renderAfterWidgets(gui);
		} catch(Throwable e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } }
	}

	private Component getWalletName() {
		ItemStack wallet = this.menu.getWallet();
		return wallet.isEmpty() ? EasyText.empty() : wallet.getHoverName();
	}
	
	public void changeTab(int tabIndex)
	{
		
		//Close the old tab
		this.currentTab().onClose();
		this.tabButtons.get(this.currentTabIndex).active = true;
		this.currentTabIndex = MathUtil.clamp(tabIndex, 0, this.tabs.size() - 1);
		this.tabButtons.get(this.currentTabIndex).active = false;
		
		//Initialize the new tab
		this.currentTab().onOpen();
		
		this.logError = true;
	}
	
	private void clickedOnTab(EasyButton tab)
	{
		if(tab instanceof TabButton)
		{
			int tabIndex = this.tabButtons.indexOf(tab);
			if(tabIndex < 0)
				return;
			this.changeTab(tabIndex);
		}
	}

	@Override
	public boolean blockInventoryClosing() { return this.currentTab().blockInventoryClosing(); }
	
	private void PressOpenWalletButton(EasyButton button) { new CPacketOpenWallet(this.menu.getWalletStackIndex()).send(); }
	
	
}
