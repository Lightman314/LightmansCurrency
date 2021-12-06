package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletConvertCoins;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageWalletToggleAutoConvert;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class WalletScreen extends AbstractContainerScreen<WalletContainer>{

	private final int BASEHEIGHT = 114;
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/wallet.png");
	
	IconButton buttonToggleAutoConvert;
	Button buttonConvert;
	boolean autoConvert = false;
	
	public WalletScreen(WalletContainer container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = BASEHEIGHT + this.menu.getRowCount() * 18;
		this.imageWidth = 176;
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		//Draw the top
		this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, 17);
		//Draw the middle strips
		for(int y = 0; y < this.menu.getRowCount(); y++)
		{
			this.blit(poseStack, this.leftPos, this.topPos + 17 + y * 18, 0, 17, this.imageWidth, 18);
		}
		//Draw the bottom
		this.blit(poseStack, this.leftPos, this.topPos + 17 + this.menu.getRowCount() * 18, 0, 35, this.imageWidth, BASEHEIGHT - 17);
		
		//Draw the slots
		for(int y = 0; y * 9 < this.menu.getSlotCount(); y++)
		{
			for(int x = 1; x < 9 && x + y * 9 < this.menu.getSlotCount(); x++)
			{
				this.blit(poseStack, this.leftPos + 7 + x * 18, this.topPos + 17 + y * 18, 0, BASEHEIGHT + 18, 18, 18);
			}
		}
		
	}
	
	@Override
	protected void renderLabels(PoseStack matrix, int mouseX, int mouseY)
	{
		this.font.draw(matrix, this.menu.title, 8.0f, 6.0f, 0x404040);
		this.font.draw(matrix, this.playerInventoryTitle, 8.0f, (this.imageHeight - 94), 0x404040);
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		if(this.menu.canConvert())
		{
			//Create the buttons
			this.buttonConvert = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos, this::PressConvertButton, GUI_TEXTURE, this.imageWidth, 0));
			
			if(this.menu.canPickup())
			{
				this.buttonToggleAutoConvert = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos + 20, this::PressAutoConvertToggleButton, GUI_TEXTURE, this.imageWidth, 16));
				this.updateToggleButton();
			}
			
		}
		
	}
	
	@Override
	public void containerTick()
	{
		
		if(this.buttonToggleAutoConvert != null)
		{
			//CurrencyMod.LOGGER.info("Local AC: " + this.autoConvert + " Stack AC: " + this.container.getAutoConvert());
			if(this.menu.getAutoConvert() != this.autoConvert)
				this.updateToggleButton();
		}
		
		super.tick();
	}
	
	private void updateToggleButton()
	{
		//CurrencyMod.LOGGER.info("Updating AutoConvert Button");
		this.autoConvert = this.menu.getAutoConvert();
		this.buttonToggleAutoConvert.setResource(GUI_TEXTURE, this.imageWidth, this.autoConvert ? 16 : 32);
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
	
	public void PressConvertButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageWalletConvertCoins());
	}
	
	public void PressAutoConvertToggleButton(Button button)
	{
		this.menu.ToggleAutoConvert();
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageWalletToggleAutoConvert());
	}
	
}
