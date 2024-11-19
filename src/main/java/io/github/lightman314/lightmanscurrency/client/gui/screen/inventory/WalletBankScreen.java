package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyClientTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketOpenWallet;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletBankScreen extends EasyClientTabbedMenuScreen<WalletBankMenu,WalletBankScreen,WalletBankTab> {

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/wallet_bank.png");
	
	EasyButton buttonOpenWallet;
	
	public WalletBankScreen(WalletBankMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.initializeTabs();
	}

	@Override
	protected void registerTabs() {
		this.addTab(new SelectionTab(this));
		this.addTab(new InteractionTab(this));
	}

	@Nonnull
	@Override
	protected IWidgetPositioner getTabButtonPositioner() {
		return LazyWidgetPositioner.create(this,LazyWidgetPositioner.createTopdown(WidgetRotation.LEFT),ScreenPosition.of(-25,0),25);
	}

	@Override
	protected void init(ScreenArea screenArea)
	{

		screenArea = this.resize(176 + this.menu.bonusWidth, WalletBankMenu.BANK_WIDGET_SPACING + this.menu.coinSlotHeight * 18 + 7);
		
		this.buttonOpenWallet = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width,0))
				.pressAction(this::PressOpenWalletButton)
				.icon(IconData.of(this.menu.getWallet()))
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_WALLET_OPEN_WALLET))
				.build());

		this.currentTab().onOpen();
		
	}
	
	@Override
	protected void renderBackground(@Nonnull EasyGuiGraphics gui) {

		gui.resetColor();
		//Draw the top
		gui.renderNormalBackground(this);

		//Draw the coin slots
		for(Slot slot : this.menu.slots)
			gui.renderSlot(this,slot);

		gui.drawString(this.getWalletName(), 8, WalletBankMenu.BANK_WIDGET_SPACING - 11, 0x404040);

	}

	private Component getWalletName() {
		ItemStack wallet = this.menu.getWallet();
		return wallet.isEmpty() ? EasyText.empty() : wallet.getHoverName();
	}
	
	private void PressOpenWalletButton(EasyButton button) { new CPacketOpenWallet(this.menu.getWalletStackIndex()).send(); }
	
	
}
