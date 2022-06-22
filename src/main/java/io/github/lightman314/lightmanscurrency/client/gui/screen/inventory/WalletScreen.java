package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget.IBankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletConvertCoins;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletToggleAutoConvert;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.menus.WalletMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

@IPNIgnore
public class WalletScreen extends AbstractContainerScreen<WalletMenu> implements IBankAccountWidget{

	private final int BASEHEIGHT = 114;
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet.png");
	
	private boolean walletSlotChanged = false;
	
	IconButton buttonToggleAutoConvert;
	Button buttonConvert;
	boolean autoConvert = false;
	BankAccountWidget bankWidget;
	
	public WalletScreen(WalletMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		container.addListener(this::onContainerReload);
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int yOffset = this.menu.getVerticalOffset();
		
		//Draw the Wallet Slot
		//this.blit(poseStack, this.leftPos - 28, this.topPos + yOffset, 18, 132, 28, 28);
		
		//Draw the top
		this.blit(poseStack, this.leftPos, this.topPos + yOffset, 0, 0, this.imageWidth, 17);
		//Draw the middle strips
		for(int y = 0; y < this.menu.getRowCount(); y++)
		{
			this.blit(poseStack, this.leftPos, this.topPos + 17 + y * 18 + yOffset, 0, 17, this.imageWidth, 18);
		}
		//Draw the bottom
		this.blit(poseStack, this.leftPos, this.topPos + 17 + this.menu.getRowCount() * 18 + yOffset, 0, 35, this.imageWidth, BASEHEIGHT - 17);
		
		//Draw the slots
		for(int y = 0; y * 9 < this.menu.getSlotCount(); y++)
		{
			for(int x = 0; x < 9 && x + y * 9 < this.menu.getSlotCount(); x++)
			{
				this.blit(poseStack, this.leftPos + 7 + x * 18, this.topPos + 17 + y * 18 + yOffset, 0, BASEHEIGHT + 18, 18, 18);
			}
		}
		
		//Draw the background of the Bank Account Widget
		if(this.menu.hasBankAccess() && this.bankWidget != null)
		{
			//Calculate the area to draw a background for
			int startX = this.width / 2 - BankAccountWidget.BUTTON_WIDTH - 10;
			int startY = this.topPos + CoinValueInput.HEIGHT;
			int width = (2 * BankAccountWidget.BUTTON_WIDTH) + 20;
			int height = BankAccountWidget.HEIGHT - CoinValueInput.HEIGHT;
			//Left edge
			int xOff = 0;
			int yOff = 0;
			while(yOff < height)
			{
				int thisHeight = MathUtil.clamp(height - yOff, 0, 32);
				this.blit(poseStack, startX, startY + yOff, 0, 8, 7, thisHeight);
				yOff += thisHeight;
			}
			//Right edge
			yOff = 0;
			while(yOff < height)
			{
				int thisHeight = MathUtil.clamp(height - yOff, 0, 32);
				this.blit(poseStack, startX + width - 7, startY + yOff, 169, 8, 7, thisHeight);
				yOff += thisHeight;
			}
			//Center fill
			while(xOff < width - 14)
			{
				yOff = 0;
				int thisWidth = MathUtil.clamp(width - xOff - 14, 0, 18);
				while(yOff < height)
				{
					int thisHeight = MathUtil.clamp(height - yOff, 0, 32);
					this.blit(poseStack, startX + 7 + xOff, startY + yOff, 7, 8, thisWidth, thisHeight);
					yOff += thisHeight;
				}
				xOff += thisWidth;
			}
			
			//Draw the text
			this.bankWidget.renderInfo(poseStack, -1);
		}
		
	}
	
	private Component getWalletName() {
		ItemStack wallet = this.menu.getWallet();
		return wallet.isEmpty() ? new TextComponent("") : wallet.getHoverName();
	}
	
	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY)
	{
		this.font.draw(pose, this.getWalletName(), 8.0f, 6.0f + this.menu.getVerticalOffset(), 0x404040);
		this.font.draw(pose, this.playerInventoryTitle, 8.0f, (this.imageHeight - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		
		int yOffset = this.menu.getVerticalOffset();
		
		this.imageHeight = BASEHEIGHT + this.menu.getRowCount() * 18 + yOffset;
		this.imageWidth = 176;
		
		
		super.init();
		
		this.clearWidgets();
		this.buttonConvert = null;
		this.buttonToggleAutoConvert = null;
		this.bankWidget = null;
		
		//this.addRenderableWidget(new VisibilityToggleButton(this.leftPos - 23, this.topPos + 5 + this.menu.getVerticalOffset(), this::isWalletVisible, this::PressToggleWalletVisibilityButton));
		
		if(this.menu.canConvert())
		{
			//Create the buttons
			this.buttonConvert = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos + yOffset, this::PressConvertButton, IconData.of(GUI_TEXTURE, this.imageWidth, 0)));
			
			if(this.menu.canPickup())
			{
				this.buttonToggleAutoConvert = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos + 20 + yOffset, this::PressAutoConvertToggleButton, IconData.of(GUI_TEXTURE, this.imageWidth, 16)));
				this.updateToggleButton();
			}
		}
		if(this.menu.hasBankAccess())
		{
			this.bankWidget = new BankAccountWidget(this.topPos, this, -1);
			this.bankWidget.allowEmptyDeposits = false;
		}
		
	}
	
	/*private boolean isWalletVisible() {
		IWalletHandler handler = WalletCapability.getWalletHandler(this.menu.getPlayer()).orElse(null);
		return handler == null ? true : handler.visible();
	}*/
	
	private void onContainerReload() {
		this.walletSlotChanged = true;
	}
	
	@Override
	public void containerTick()
	{
		
		this.menu.clientTick();
		
		if(this.walletSlotChanged) {
			this.walletSlotChanged = false;
			this.init();
		}
		
		if(this.buttonToggleAutoConvert != null)
		{
			//CurrencyMod.LOGGER.info("Local AC: " + this.autoConvert + " Stack AC: " + this.container.getAutoConvert());
			if(this.menu.getAutoConvert() != this.autoConvert)
				this.updateToggleButton();
		}
		if(this.bankWidget != null)
		{
			this.bankWidget.tick();
		}
		
	}
	
	private void updateToggleButton()
	{
		//CurrencyMod.LOGGER.info("Updating AutoConvert Button");
		this.autoConvert = this.menu.getAutoConvert();
		this.buttonToggleAutoConvert.setIcon(IconData.of(GUI_TEXTURE, this.imageWidth, this.autoConvert ? 16 : 32));
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonConvert != null && this.buttonConvert.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.wallet.convert"), mouseX, mouseY);
		}
		else if(this.buttonToggleAutoConvert != null && this.buttonToggleAutoConvert.isMouseOver(mouseX, mouseY))
		{
			if(this.autoConvert)
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.wallet.autoconvert.disable"), mouseX, mouseY);
			else
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.wallet.autoconvert.enable"), mouseX, mouseY);
		}
	}
	
	private void PressConvertButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageWalletConvertCoins());
	}
	
	private void PressAutoConvertToggleButton(Button button)
	{
		this.menu.ToggleAutoConvert();
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageWalletToggleAutoConvert());
	}

	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T widget) {
		return this.addRenderableWidget(widget);
	}

	@Override
	public Font getFont() {
		return this.font;
	}

	@Override
	public Screen getScreen() {
		return this;
	}

	@Override
	public BankAccount getAccount() {
		return this.menu.getAccount();
	}

	@Override
	public Container getCoinAccess() {
		return this.menu.getCoinInput();
	}
	
}
