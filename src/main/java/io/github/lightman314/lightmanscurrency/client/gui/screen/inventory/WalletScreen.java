package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletConvertCoins;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletToggleAutoConvert;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
//import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
//import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class WalletScreen extends ContainerScreen<WalletContainer>{

	private final int BASEHEIGHT = 114;
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet.png");
	
	IconButton buttonToggleAutoConvert;
	Button buttonConvert;
	boolean autoConvert = false;
	
	public WalletScreen(WalletContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.ySize = BASEHEIGHT + this.container.getRowCount() * 18;
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
		
		//Draw the top
		this.blit(matrix, startX, startY, 0, 0, this.xSize, 17);
		//Draw the middle strips
		for(int y = 0; y < this.container.getRowCount(); y++)
		{
			this.blit(matrix, startX, startY + 17 + y * 18, 0, 17, this.xSize, 18);
		}
		//Draw the bottom
		this.blit(matrix, startX, startY + 17 + this.container.getRowCount() * 18, 0, 35, this.xSize, BASEHEIGHT - 17);
		
		//Draw the slots
		for(int y = 0; y * 9 < this.container.getSlotCount(); y++)
		{
			for(int x = 1; x < 9 && x + y * 9 < this.container.getSlotCount(); x++)
			{
				this.blit(matrix, startX + 7 + x * 18, startY + 17 + y * 18, 0, BASEHEIGHT + 18, 18, 18);
			}
		}
		
		CoinSlot.drawEmptyCoinSlots(this, this.container, matrix, startX, startY);
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		this.font.drawString(matrix, this.container.title.getString(), 8.0f, 6.0f, 0x404040);
		this.font.drawString(matrix, this.playerInventory.getDisplayName().getString(), 8.0f, (this.ySize - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		if(this.container.canConvert())
		{
			//Create the buttons
			this.buttonConvert = this.addButton(new IconButton(this.guiLeft - 20, this.guiTop, this::PressConvertButton, GUI_TEXTURE, this.xSize, 0));
			
			if(this.container.canPickup())
			{
				this.buttonToggleAutoConvert = this.addButton(new IconButton(this.guiLeft - 20, this.guiTop + 20, this::PressAutoConvertToggleButton, GUI_TEXTURE, this.xSize, 16));
				this.updateToggleButton();
			}
			
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
		
		super.tick();
	}
	
	private void updateToggleButton()
	{
		//CurrencyMod.LOGGER.info("Updating AutoConvert Button");
		this.autoConvert = this.container.getAutoConvert();
		this.buttonToggleAutoConvert.setResource(GUI_TEXTURE, this.xSize, this.autoConvert ? 16 : 32);
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
	
}
