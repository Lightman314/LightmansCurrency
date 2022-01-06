package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.paygate.MessageActivatePaygate;
import io.github.lightman314.lightmanscurrency.network.message.paygate.MessageSetPaygateTicket;
import io.github.lightman314.lightmanscurrency.network.message.paygate.MessageUpdatePaygateData;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.menus.PaygateMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.PaygateBlockEntity;

public class PaygateScreen extends AbstractContainerScreen<PaygateMenu> implements ICoinValueInput{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/paygate.png");
	
	private static final int GUI_HEIGHT = 151;
	
	CoinValueInput priceInput;
	EditBox durationInput;
	
	private IconButton buttonCollectMoney;
	private IconButton buttonPay;
	private IconButton buttonSetTicket;
	
	public PaygateScreen(PaygateMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = this.menu.isOwner() ? GUI_HEIGHT + CoinValueInput.HEIGHT : GUI_HEIGHT;
		this.imageWidth = 176;
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		if(this.menu.isOwner())
			this.blit(poseStack, this.leftPos, this.topPos + this.menu.priceInputOffset, 0, 0, this.imageWidth, this.imageHeight - this.menu.priceInputOffset);
		else
			this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		this.font.draw(poseStack, this.title, 8.0f, 6.0f + this.menu.priceInputOffset, 0x404040);
		this.font.draw(poseStack, this.playerInventoryTitle, 8.0f, (this.imageHeight - 94), 0x404040);
		
		this.font.draw(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.paygate.price", this.menu.tileEntity.getPrice().getString()).getString(), 8f, 16f + this.menu.priceInputOffset, 0x000000);
		this.font.draw(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.paygate.duration", this.menu.tileEntity.getDuration()).getString(), 8f, 25f + this.menu.priceInputOffset, 0x000000);
		
		this.font.draw(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.menu.GetCoinValue())).getString(), 80f, this.imageHeight - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		if(this.menu.isOwner())
		{
			
			this.priceInput = this.addRenderableWidget(new CoinValueInput(this.topPos, new TranslatableComponent("gui.lightmanscurrency.changeprice"), this.menu.tileEntity.getPrice(), this));
			this.priceInput.init();
			
			this.durationInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 8, this.topPos + 35 + this.menu.priceInputOffset, 30, 18, new TextComponent("")));
			this.durationInput.setValue(String.valueOf(this.menu.tileEntity.getDuration()));
			this.durationInput.setMaxLength(3);
			
			this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos + this.menu.priceInputOffset, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, this.imageWidth + 16, 0)));
			this.buttonCollectMoney.active = false;
			
			this.buttonSetTicket = this.addRenderableWidget(new IconButton(this.leftPos + 40, this.topPos + 34 + this.menu.priceInputOffset, this::PressTicketButton, this.font, IconData.of(GUI_TEXTURE, this.imageWidth + 32, 0)));
			this.buttonSetTicket.visible = false;
			
		}
		
		this.buttonPay = this.addRenderableWidget((new IconButton(this.leftPos + 149, this.topPos + 6 + this.menu.priceInputOffset, this::PressActivateButton, this.font, IconData.of(GUI_TEXTURE, this.imageWidth, 0))));
		this.buttonPay.active = false;
		
		tick();
		
	}
	
	@Override
	public void containerTick()
	{
		
		if(this.priceInput != null)
			this.priceInput.tick();
		
		if(this.durationInput != null)
		{
			this.durationInput.tick();
			int duration = this.getDuration();
			
			if(duration != menu.tileEntity.getDuration())
			{
				//CurrencyMod.LOGGER.info("Sending update paygate data message to the server.");
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdatePaygateData(this.menu.tileEntity.getBlockPos(), this.priceInput.getCoinValue().copy(), duration));
				
				this.menu.tileEntity.setDuration(duration);
				
				if(this.durationInput.getValue() != "")
					this.durationInput.setValue(String.valueOf(this.menu.tileEntity.getDuration()));
				
			}
		}
		
		if(this.buttonCollectMoney != null)
		{
			this.buttonCollectMoney.active = this.menu.tileEntity.getStoredMoney().getRawValue() > 0;
		}
		
		if(this.buttonSetTicket != null)
		{
			this.buttonSetTicket.visible = this.menu.HasMasterTicket() && !this.menu.tileEntity.validTicket(this.menu.GetTicketID());
		}
		
		this.buttonPay.active = this.menu.CanActivate();

	}
	
	
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		this.renderBackground(matrixStack);
		//Render price input before buttons
		if(this.priceInput != null)
			this.priceInput.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(this.durationInput != null)
			this.durationInput.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonPay != null && this.buttonPay.active && this.buttonPay.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.paygate.paybutton"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonSetTicket != null && this.buttonSetTicket.visible && this.buttonSetTicket.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.paygate.setticket", this.menu.tileEntity.getStoredMoney()), mouseX, mouseY);
		}
		
	}
	
	private void PressActivateButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageActivatePaygate());
	}
	
	private void PressCollectionButton(Button button)
	{
		if(this.menu.isOwner())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
	}
	
	private void PressTicketButton(Button button)
	{
		if(this.menu.isOwner())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetPaygateTicket(this.menu.tileEntity.getBlockPos(), this.menu.GetTicketID()));
	}
	
	private int getDuration()
	{
		return MathUtil.clamp(TextInputUtil.getIntegerValue(this.durationInput), PaygateBlockEntity.DURATION_MIN, PaygateBlockEntity.DURATION_MAX);
	}

	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T widget) {
		return this.addRenderableWidget(widget);
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public void OnCoinValueChanged(CoinValueInput input) {
		
		this.menu.tileEntity.setPrice(input.getCoinValue());
		
		int duration = this.getDuration();
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdatePaygateData(this.menu.tileEntity.getBlockPos(), this.priceInput.getCoinValue().copy(), duration));
		
	}

	@Override
	public Font getFont() {
		return this.font;
	}
	
}
