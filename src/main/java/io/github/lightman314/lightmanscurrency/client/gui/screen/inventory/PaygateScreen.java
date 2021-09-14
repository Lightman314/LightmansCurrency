package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.containers.PaygateContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.TicketSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.paygate.MessageActivatePaygate;
import io.github.lightman314.lightmanscurrency.network.message.paygate.MessageSetPaygateTicket;
import io.github.lightman314.lightmanscurrency.network.message.paygate.MessageUpdatePaygateData;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.PaygateBlockEntity;



public class PaygateScreen extends AbstractContainerScreen<PaygateContainer> implements ICoinValueInput{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/paygate.png");
	
	private static final int GUI_HEIGHT = 151;
	
	CoinValueInput priceInput;
	EditBox durationInput;
	
	private IconButton buttonCollectMoney;
	private IconButton buttonPay;
	private IconButton buttonSetTicket;
	
	public PaygateScreen(PaygateContainer container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = this.menu.isOwner() ? GUI_HEIGHT + CoinValueInput.HEIGHT : GUI_HEIGHT;
		this.imageWidth = 176;
	}
	
	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		//RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		//this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		int startX = (this.width - this.imageWidth) / 2;
		int startY = (this.height - this.imageHeight) / 2;
		
		if(this.menu.isOwner())
			this.blit(matrix, startX, startY + this.menu.priceInputOffset, 0, 0, this.imageWidth, this.imageHeight - this.menu.priceInputOffset);
		else
			this.blit(matrix, startX, startY, 0, 0, this.imageWidth, this.imageHeight);
		
		CoinSlot.drawEmptyCoinSlots(this, this.menu, matrix, startX, startY);
		TicketSlot.drawEmptyTicketSlots(this, this.menu, matrix, startX, startY);
		
	}
	
	@Override
	protected void renderLabels(PoseStack matrix, int mouseX, int mouseY)
	{
		this.font.draw(matrix, this.title.getString(), 8.0f, 6.0f + this.menu.priceInputOffset, 0x404040);
		this.font.draw(matrix, this.playerInventoryTitle.getString(), 8.0f, (this.imageHeight - 94), 0x404040);
		
		this.font.draw(matrix, new TranslatableComponent("tooltip.lightmanscurrency.paygate.price", this.menu.blockEntity.getPrice().getString()).getString(), 8f, 16f + this.menu.priceInputOffset, 0x000000);
		this.font.draw(matrix, new TranslatableComponent("tooltip.lightmanscurrency.paygate.duration", this.menu.blockEntity.getDuration()).getString(), 8f, 25f + this.menu.priceInputOffset, 0x000000);
		
		this.font.draw(matrix, new TranslatableComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.menu.GetCoinValue())).getString(), 80f, this.imageHeight - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		if(this.menu.isOwner())
		{
			
			this.priceInput = this.addWidget(new CoinValueInput(this.topPos, new TranslatableComponent("gui.lightmanscurrency.changeprice"), this.menu.blockEntity.getPrice(), this));
			
			this.durationInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 8, this.topPos + 35 + this.menu.priceInputOffset, 30, 18, TextComponent.EMPTY));
			this.durationInput.setValue(String.valueOf(this.menu.blockEntity.getDuration()));
			this.durationInput.setMaxLength(3);
			
			this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos + this.menu.priceInputOffset, this::PressCollectionButton, GUI_TEXTURE, this.imageWidth + 16, 0));
			this.buttonCollectMoney.active = false;
			
			this.buttonSetTicket = this.addRenderableWidget(new IconButton(this.leftPos + 40, this.topPos + 34 + this.menu.priceInputOffset, this::PressTicketButton, GUI_TEXTURE, this.imageWidth + 32, 0));
			this.buttonSetTicket.visible = false;
			
		}
		
		this.buttonPay = this.addRenderableWidget(new IconButton(this.leftPos + 149, this.topPos + 6 + this.menu.priceInputOffset, this::PressActivateButton, GUI_TEXTURE, this.imageWidth, 0));
		this.buttonPay.active = false;
		
		containerTick();
		
	}
	
	@Override
	public void containerTick()
	{
		
		if(this.priceInput != null)
			this.priceInput.tick();
		
		if(this.durationInput != null)
		{
			this.durationInput.tick();
			int duration = MathUtil.clamp(inputValue(this.durationInput), PaygateBlockEntity.DURATION_MIN, PaygateBlockEntity.DURATION_MAX);
			
			if(duration != menu.blockEntity.getDuration())
			{
				//CurrencyMod.LOGGER.info("Sending update paygate data message to the server.");
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdatePaygateData(this.menu.blockEntity.getBlockPos(), this.priceInput.getCoinValue().copy(), duration));
				
				this.menu.blockEntity.setDuration(duration);
				
				if(this.durationInput.getValue() != "")
					this.durationInput.setValue(String.valueOf(this.menu.blockEntity.getDuration()));
				
			}
		}
		
		if(this.buttonCollectMoney != null)
		{
			this.buttonCollectMoney.active = this.menu.blockEntity.getStoredMoney().getRawValue() > 0;
		}
		
		if(this.buttonSetTicket != null)
		{
			this.buttonSetTicket.visible = this.menu.HasMasterTicket() && !this.menu.blockEntity.validTicket(this.menu.GetTicketID());
		}
		
		this.buttonPay.active = (this.menu.GetCoinValue() >= this.menu.blockEntity.getPrice().getRawValue() || this.menu.HasValidTicket()) && !this.menu.blockEntity.isActive();

	}
	
	
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		this.renderBackground(matrixStack);
		//Render price input before buttons
		if(this.priceInput != null)
			this.priceInput.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		//if(this.durationInput != null)
		//	this.durationInput.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonPay != null && this.buttonPay.active && this.buttonPay.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.paygate.paybutton"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.blockEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonSetTicket != null && this.buttonSetTicket.visible && this.buttonSetTicket.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.paygate.setticket", this.menu.blockEntity.getStoredMoney()), mouseX, mouseY);
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
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetPaygateTicket(this.menu.blockEntity.getBlockPos(), this.menu.GetTicketID()));
	}
	
	private int inputValue(EditBox textField)
	{
		if(isNumeric(textField.getValue()))
			return Integer.parseInt(textField.getValue());
		return 0;
	}
	
	private static boolean isNumeric(String string)
	{
		if(string == null)
			return false;
		try
		{
			@SuppressWarnings("unused")
			int i = Integer.parseInt(string);
		} 
		catch(NumberFormatException nfe)
		{
			return false;
		}
		return true;
	}

	@Override
	public <T extends AbstractWidget> T addCustomWidget(T widget) {
		return super.addRenderableWidget(widget);
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public void OnCoinValueChanged(CoinValueInput input) {
		
		this.menu.blockEntity.setPrice(input.getCoinValue());
		
		int duration = MathUtil.clamp(inputValue(this.durationInput), PaygateBlockEntity.DURATION_MIN, PaygateBlockEntity.DURATION_MAX);
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdatePaygateData(this.menu.blockEntity.getPos(), this.priceInput.getCoinValue().copy(), duration));
		
	}

	@Override
	public Font getFont() {
		return this.font;
	}
	
}
