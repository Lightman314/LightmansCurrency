package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketOpenWalletBank;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketWalletExchangeCoins;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketWalletQuickCollect;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketWalletToggleAutoExchange;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletScreen extends EasyMenuScreen<WalletMenu> {
	
	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/wallet.png");

	IconButton buttonToggleAutoExchange;
	EasyButton buttonExchange;

	EasyButton buttonOpenBank;

	EasyButton buttonQuickCollect;

	private final LazyWidgetPositioner positioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.createTopdown(), -20, 0, 20);

	public WalletScreen(WalletMenu container, Inventory inventory, Component title) { super(container, inventory, title); }

	@Override
	protected void initialize(ScreenArea screenArea)
	{

		screenArea = this.resize(176 + this.menu.bonusWidth, 114 + this.menu.coinSlotHeight * 18);

		this.buttonExchange = null;
		this.buttonToggleAutoExchange = null;

		this.addChild(this.positioner);
		this.positioner.clear();

		//Create the buttons
		this.buttonExchange = this.addChild(IconButton.builder()
				.pressAction(this::PressExchangeButton)
				.icon(IconData.of(GUI_TEXTURE,176,0))
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_WALLET_EXCHANGE))
				.addon(EasyAddonHelper.visibleCheck(this.menu::canExchange))
				.build());

		this.buttonToggleAutoExchange = this.addChild(IconButton.builder()
				.pressAction(this::PressAutoExchangeToggleButton)
				.icon(this::getAutoExchangeIcon)
				.addon(EasyAddonHelper.tooltip(this::getAutoExchangeTooltip))
				.addon(EasyAddonHelper.visibleCheck(() -> this.menu.canExchange() && this.menu.canPickup()))
				.build());

		this.buttonOpenBank = this.addChild(IconButton.builder()
				.pressAction(this::PressOpenBankButton)
				.icon(IconData.of(ModBlocks.ATM))
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_WALLET_OPEN_BANK))
				.addon(EasyAddonHelper.visibleCheck(() -> this.menu.hasBankAccess() && !QuarantineAPI.IsDimensionQuarantined(this.menu.player)))
				.build());
		this.positioner.addWidgets(this.buttonExchange,this.buttonToggleAutoExchange,this.buttonOpenBank);

		this.buttonQuickCollect = this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(159 + this.menu.halfBonusWidth,screenArea.height - 95))
				.pressAction(this::PressQuickCollectButton)
				.sprite(IconAndButtonUtil.SPRITE_QUICK_INSERT)
				.build());

	}

	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui)
	{

		gui.resetColor();
		//Draw the background
		gui.renderNormalBackground(this);
		//Draw each slot
		for(Slot slot : this.menu.slots)
			gui.renderSlot(this,slot);

		gui.drawString(this.getWalletName(), 8, 6, 0x404040);
		gui.drawString(this.playerInventoryTitle, 8 + this.menu.halfBonusWidth, (this.getYSize() - 94), 0x404040);
		
	}
	
	private Component getWalletName() {
		ItemStack wallet = this.menu.getWallet();
		return wallet.isEmpty() ? EasyText.empty() : wallet.getHoverName();
	}

	private IconData getAutoExchangeIcon() {
		return IconData.of(GUI_TEXTURE,176,this.menu.getAutoExchange() ? 16 : 32);
	}

	private Component getAutoExchangeTooltip() { return this.menu.getAutoExchange() ? LCText.TOOLTIP_WALLET_AUTO_EXCHANGE_DISABLE.get() : LCText.TOOLTIP_WALLET_AUTO_EXCHANGE_ENABLE.get(); }
	
	private void PressExchangeButton(EasyButton button)
	{
		CPacketWalletExchangeCoins.sendToServer();
	}
	
	private void PressAutoExchangeToggleButton(EasyButton button)
	{
		this.menu.ToggleAutoExchange();
		CPacketWalletToggleAutoExchange.sendToServer();
	}
	
	private void PressOpenBankButton(EasyButton button)
	{
		new CPacketOpenWalletBank(this.menu.getWalletStackIndex()).send();
	}
	
	private void PressQuickCollectButton(EasyButton button)
	{
		CPacketWalletQuickCollect.sendToServer();
	}
	
}
