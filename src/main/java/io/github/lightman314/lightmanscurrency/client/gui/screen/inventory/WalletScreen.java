package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

import javax.annotation.Nonnull;

public class WalletScreen extends EasyMenuScreen<WalletMenu> {

	private final int BASEHEIGHT = 114;
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet.png");

	public static final Sprite SPRITE_QUICK_COLLECT = Sprite.SimpleSprite(GUI_TEXTURE, 192, 0, 10, 10);

	IconButton buttonToggleAutoExchange;
	EasyButton buttonExchange;
	boolean autoExchange = false;

	EasyButton buttonOpenBank;

	EasyButton buttonQuickCollect;

	private final LazyWidgetPositioner positioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_TOPDOWN, -20, 0, 20);

	public WalletScreen(WalletMenu container, Inventory inventory, Component title) { super(container, inventory, title); }

	@Override
	protected void initialize(ScreenArea screenArea)
	{

		screenArea = this.resize(176, BASEHEIGHT + this.menu.getRowCount() * 18);

		this.buttonExchange = null;
		this.buttonToggleAutoExchange = null;

		this.addChild(this.positioner);
		this.positioner.clear();

		if(this.menu.canExchange())
		{
			//Create the buttons
			this.buttonExchange = this.addChild(new IconButton(ScreenPosition.ZERO, this::PressExchangeButton, IconData.of(GUI_TEXTURE, this.imageWidth, 0))
					.withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_WALLET_EXCHANGE)));

			this.positioner.addWidget(this.buttonExchange);

			if(this.menu.canPickup())
			{
				this.buttonToggleAutoExchange = this.addChild(new IconButton(ScreenPosition.ZERO, this::PressAutoExchangeToggleButton, IconData.of(GUI_TEXTURE, this.imageWidth, 16))
						.withAddons(EasyAddonHelper.tooltip(this::getAutoExchangeTooltip)));
				this.updateToggleButton();
				this.positioner.addWidget(this.buttonToggleAutoExchange);
			}
		}

		if(this.menu.hasBankAccess())
		{
			this.buttonOpenBank = this.addChild(new IconButton(ScreenPosition.ZERO, this::PressOpenBankButton, IconData.of(ModBlocks.ATM.get().asItem()))
					.withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_WALLET_OPEN_BANK)));
			this.positioner.addWidget(this.buttonOpenBank);
		}

		this.buttonQuickCollect = this.addChild(new PlainButton(screenArea.pos.offset( 159, screenArea.height - 95), this::PressQuickCollectButton, SPRITE_QUICK_COLLECT));

	}

	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui)
	{

		gui.resetColor();
		//Draw the top
		gui.blit(GUI_TEXTURE, 0, 0, 0, 0, this.imageWidth, 17);
		//Draw the middle strips
		for(int y = 0; y < this.menu.getRowCount(); y++)
			gui.blit(GUI_TEXTURE, 0, 17 + y * 18, 0, 17, this.imageWidth, 18);
		//Draw the bottom
		gui.blit(GUI_TEXTURE, 0, 17 + this.menu.getRowCount() * 18, 0, 35, this.imageWidth, BASEHEIGHT - 17);
		
		//Draw the slots
		for(int y = 0; y * 9 < this.menu.getSlotCount(); y++)
		{
			for(int x = 0; x < 9 && x + y * 9 < this.menu.getSlotCount(); x++)
			{
				gui.blit(GUI_TEXTURE, 7 + x * 18, 17 + y * 18, 0, BASEHEIGHT + 18, 18, 18);
			}
		}

		gui.drawString(this.getWalletName(), 8, 6, 0x404040);
		gui.drawString(this.playerInventoryTitle, 8, (this.getYSize() - 94), 0x404040);
		
	}
	
	private Component getWalletName() {
		ItemStack wallet = this.menu.getWallet();
		return wallet.isEmpty() ? EasyText.empty() : wallet.getHoverName();
	}
	
	@Override
	public void screenTick()
	{
		
		if(this.buttonToggleAutoExchange != null)
		{
			//CurrencyMod.LOGGER.info("Local AC: " + this.autoConvert + " Stack AC: " + this.container.getAutoExchange());
			if(this.menu.getAutoExchange() != this.autoExchange)
				this.updateToggleButton();
		}
		
	}
	
	private void updateToggleButton()
	{
		//CurrencyMod.LOGGER.info("Updating AutoConvert Button");
		this.autoExchange = this.menu.getAutoExchange();
		this.buttonToggleAutoExchange.setIcon(IconData.of(GUI_TEXTURE, this.imageWidth, this.autoExchange ? 16 : 32));
	}

	private Component getAutoExchangeTooltip() { return this.autoExchange ? LCText.TOOLTIP_WALLET_AUTO_EXCHANGE_DISABLE.get() : LCText.TOOLTIP_WALLET_AUTO_EXCHANGE_ENABLE.get(); }
	
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
