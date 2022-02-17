package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget.IBankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletConvertCoins;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletToggleAutoConvert;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

@IPNIgnore
public class WalletScreen extends ContainerScreen<WalletContainer> implements IBankAccountWidget{

	private final int BASEHEIGHT = 114;
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet.png");
	
	IconButton buttonToggleAutoConvert;
	Button buttonConvert;
	boolean autoConvert = false;
	BankAccountWidget bankWidget;
	
	public WalletScreen(WalletContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		container.addListener(this::init);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		
		int yOffset = this.container.getVerticalOffset();
		
		//Draw the Wallet Slot
		this.blit(matrix, startX - 28, startY + yOffset, 18, 132, 28, 28);
		
		//Draw the top
		this.blit(matrix, startX, startY + yOffset, 0, 0, this.xSize, 17);
		//Draw the middle strips
		for(int y = 0; y < this.container.getRowCount(); y++)
		{
			this.blit(matrix, startX, startY + 17 + y * 18 + yOffset, 0, 17, this.xSize, 18);
		}
		//Draw the bottom
		this.blit(matrix, startX, startY + 17 + this.container.getRowCount() * 18 + yOffset, 0, 35, this.xSize, BASEHEIGHT - 17);
		
		//Draw the slots
		for(int y = 0; y * 9 < this.container.getSlotCount(); y++)
		{
			for(int x = 0; x < 9 && x + y * 9 < this.container.getSlotCount(); x++)
			{
				this.blit(matrix, startX + 7 + x * 18, startY + 17 + y * 18 + yOffset, 0, BASEHEIGHT + 18, 18, 18);
			}
		}
		
		//Draw the background of the Bank Account Widget
		if(this.container.hasBankAccess() && this.bankWidget != null)
		{
			//Render the coin value widget
			this.bankWidget.renderCoinValueWidget(matrix, mouseX, mouseY, partialTicks);
			
			this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
			//Calculate the area to draw a background for
			startX = this.width / 2 - BankAccountWidget.BUTTON_WIDTH - 10;
			startY = this.guiTop + CoinValueInput.HEIGHT;
			int width = (2 * BankAccountWidget.BUTTON_WIDTH) + 20;
			int height = BankAccountWidget.HEIGHT - CoinValueInput.HEIGHT;
			//Left edge
			int xOff = 0;
			int yOff = 0;
			while(yOff < height)
			{
				int thisHeight = MathUtil.clamp(height - yOff, 0, 32);
				this.blit(matrix, startX, startY + yOff, 0, 8, 7, thisHeight);
				yOff += thisHeight;
			}
			//Right edge
			yOff = 0;
			while(yOff < height)
			{
				int thisHeight = MathUtil.clamp(height - yOff, 0, 32);
				this.blit(matrix, startX + width - 7, startY + yOff, 169, 8, 7, thisHeight);
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
					this.blit(matrix, startX + 7 + xOff, startY + yOff, 7, 8, thisWidth, thisHeight);
					yOff += thisHeight;
				}
				xOff += thisWidth;
			}

			//Draw the text
			this.bankWidget.renderInfo(matrix, -1);
		}
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		this.font.drawString(matrix, this.container.getTitle().getString(), 8.0f, 6.0f + this.container.getVerticalOffset(), 0x404040);
		this.font.drawString(matrix, this.playerInventory.getDisplayName().getString(), 8.0f, (this.ySize - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		
		this.buttons.clear();
		this.children.clear();
		this.buttonConvert = null;
		this.buttonToggleAutoConvert = null;
		this.bankWidget = null;
		
		int yOffset = this.container.getVerticalOffset();
		this.ySize = BASEHEIGHT + this.container.getRowCount() * 18 + yOffset;
		this.xSize = 176;
		
		super.init();
		
		if(this.container.canConvert())
		{
			//Create the buttons
			this.buttonConvert = this.addButton(new IconButton(this.guiLeft - 20, this.guiTop + 28 + yOffset, this::PressConvertButton, IconData.of(GUI_TEXTURE, this.xSize, 0)));
			
			if(this.container.canPickup())
			{
				this.buttonToggleAutoConvert = this.addButton(new IconButton(this.guiLeft - 20, this.guiTop + 48 + yOffset, this::PressAutoConvertToggleButton, IconData.of(GUI_TEXTURE, this.xSize, 16)));
				this.updateToggleButton();
			}
			
		}
		
		if(this.container.hasBankAccess())
		{
			this.bankWidget = new BankAccountWidget(this.guiTop, this, -1);
			this.bankWidget.allowEmptyDeposits = false;
		}
		
	}
	
	
	
	@Override
	public void tick()
	{
		
		if(this.buttonToggleAutoConvert != null)
		{
			//CurrencyMod.LOGGER.info("Local AC: " + this.autoConvert + " Stack AC: " + this.container.getAutoConvert());
			if(this.container.getAutoConvert() != this.autoConvert)
				this.updateToggleButton();
		}
		
		if(this.bankWidget != null)
			this.bankWidget.tick();
		
		super.tick();
	}
	
	private void updateToggleButton()
	{
		//CurrencyMod.LOGGER.info("Updating AutoConvert Button");
		this.autoConvert = this.container.getAutoConvert();
		this.buttonToggleAutoConvert.setIcon(IconData.of(GUI_TEXTURE, this.xSize, this.autoConvert ? 16 : 32));
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonConvert != null && this.buttonConvert.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.wallet.convert"), mouseX, mouseY);
		}
		else if(this.buttonToggleAutoConvert != null && this.buttonToggleAutoConvert.isMouseOver(mouseX, mouseY))
		{
			if(this.autoConvert)
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.wallet.autoconvert.disable"), mouseX, mouseY);
			else
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.wallet.autoconvert.enable"), mouseX, mouseY);
		}
	}
	
	public void PressConvertButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageWalletConvertCoins());
	}
	
	public void PressAutoConvertToggleButton(Button button)
	{
		this.container.ToggleAutoConvert();
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageWalletToggleAutoConvert());
	}

	@Override
	public <T extends Button> T addCustomWidget(T widget) {
		return this.addButton(widget);
	}
	
	@Override
	public <T extends IGuiEventListener> T addCustomListener(T widget) {
		return this.addListener(widget);
	}

	@Override
	public FontRenderer getFont() {
		return this.font;
	}

	@Override
	public Screen getScreen() {
		return this;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public BankAccount getAccount() {
		return this.container.getAccount();
	}

	@Override
	public IInventory getCoinAccess() {
		return this.container.getCoinInput();
	}
	
}
