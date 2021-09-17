package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

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
import io.github.lightman314.lightmanscurrency.tileentity.PaygateTileEntity;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;



public class PaygateScreen extends ContainerScreen<PaygateContainer> implements ICoinValueInput{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/paygate.png");
	
	private static final int GUI_HEIGHT = 151;
	
	CoinValueInput priceInput;
	TextFieldWidget durationInput;
	
	private IconButton buttonCollectMoney;
	private IconButton buttonPay;
	private IconButton buttonSetTicket;
	
	public PaygateScreen(PaygateContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.ySize = this.container.isOwner() ? GUI_HEIGHT + CoinValueInput.HEIGHT : GUI_HEIGHT;
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
		
		if(this.container.isOwner())
			this.blit(matrix, startX, startY + this.container.priceInputOffset, 0, 0, this.xSize, this.ySize - this.container.priceInputOffset);
		else
			this.blit(matrix, startX, startY, 0, 0, this.xSize, this.ySize);
		
		CoinSlot.drawEmptyCoinSlots(this, this.container, matrix, startX, startY);
		TicketSlot.drawEmptyTicketSlots(this, this.container, matrix, startX, startY);
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		this.font.drawString(matrix, this.title.getString(), 8.0f, 6.0f + this.container.priceInputOffset, 0x404040);
		this.font.drawString(matrix, this.playerInventory.getDisplayName().getString(), 8.0f, (this.ySize - 94), 0x404040);
		
		this.font.drawString(matrix, new TranslationTextComponent("tooltip.lightmanscurrency.paygate.price", this.container.tileEntity.getPrice().getString()).getString(), 8f, 16f + this.container.priceInputOffset, 0x000000);
		this.font.drawString(matrix, new TranslationTextComponent("tooltip.lightmanscurrency.paygate.duration", this.container.tileEntity.getDuration()).getString(), 8f, 25f + this.container.priceInputOffset, 0x000000);
		
		this.font.drawString(matrix, new TranslationTextComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.container.GetCoinValue())).getString(), 80f, this.ySize - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		if(this.container.isOwner())
		{
			
			this.priceInput = new CoinValueInput(this.guiTop, new TranslationTextComponent("gui.lightmanscurrency.changeprice"), this.container.tileEntity.getPrice(), this);
			this.children.add(this.priceInput);
			
			this.durationInput = new TextFieldWidget(this.font, guiLeft + 8, guiTop + 35 + this.container.priceInputOffset, 30, 18, ITextComponent.getTextComponentOrEmpty(""));
			this.durationInput.setText(String.valueOf(this.container.tileEntity.getDuration()));
			this.durationInput.setMaxStringLength(3);
			this.children.add(this.durationInput);
			
			this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft - 20, this.guiTop + this.container.priceInputOffset, this::PressCollectionButton, GUI_TEXTURE, this.xSize + 16, 0));
			this.buttonCollectMoney.active = false;
			
			this.buttonSetTicket = this.addButton(new IconButton(this.guiLeft + 40, this.guiTop + 34 + this.container.priceInputOffset, this::PressTicketButton, GUI_TEXTURE, this.xSize + 32, 0));
			this.buttonSetTicket.visible = false;
			
		}
		
		this.buttonPay = this.addButton((new IconButton(this.guiLeft + 149, this.guiTop + 6 + this.container.priceInputOffset, this::PressActivateButton, GUI_TEXTURE, this.xSize, 0)));
		this.buttonPay.active = false;
		
		tick();
		
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		if(this.priceInput != null)
			this.priceInput.tick();
		
		if(this.durationInput != null)
		{
			this.durationInput.tick();
			int duration = MathUtil.clamp(inputValue(this.durationInput), PaygateTileEntity.DURATION_MIN, PaygateTileEntity.DURATION_MAX);
			
			if(duration != container.tileEntity.getDuration())
			{
				//CurrencyMod.LOGGER.info("Sending update paygate data message to the server.");
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdatePaygateData(this.container.tileEntity.getPos(), this.priceInput.getCoinValue().copy(), duration));
				
				this.container.tileEntity.setDuration(duration);
				
				if(this.durationInput.getText() != "")
					this.durationInput.setText(String.valueOf(this.container.tileEntity.getDuration()));
				
			}
		}
		
		if(this.buttonCollectMoney != null)
		{
			this.buttonCollectMoney.active = this.container.tileEntity.getStoredMoney().getRawValue() > 0;
		}
		
		if(this.buttonSetTicket != null)
		{
			this.buttonSetTicket.visible = this.container.HasMasterTicket() && !this.container.tileEntity.validTicket(this.container.GetTicketID());
		}
		
		this.buttonPay.active = this.container.CanActivate();

	}
	
	
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		this.renderBackground(matrixStack);
		//Render price input before buttons
		if(this.priceInput != null)
			this.priceInput.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(this.durationInput != null)
			this.durationInput.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonPay != null && this.buttonPay.active && this.buttonPay.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.paygate.paybutton"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.collectcoins", this.container.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonSetTicket != null && this.buttonSetTicket.visible && this.buttonSetTicket.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.paygate.setticket", this.container.tileEntity.getStoredMoney()), mouseX, mouseY);
		}
		
	}
	
	private void PressActivateButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageActivatePaygate());
	}
	
	private void PressCollectionButton(Button button)
	{
		if(this.container.isOwner())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
	}
	
	private void PressTicketButton(Button button)
	{
		if(this.container.isOwner())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetPaygateTicket(this.container.tileEntity.getPos(), this.container.GetTicketID()));
	}
	
	private int inputValue(TextFieldWidget textField)
	{
		if(isNumeric(textField.getText()))
			return Integer.parseInt(textField.getText());
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
	public <T extends Button> T addButton(T button) {
		return super.addButton(button);
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public void OnCoinValueChanged(CoinValueInput input) {
		
		this.container.tileEntity.setPrice(input.getCoinValue());
		
		int duration = MathUtil.clamp(inputValue(this.durationInput), PaygateTileEntity.DURATION_MIN, PaygateTileEntity.DURATION_MAX);
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdatePaygateData(this.container.tileEntity.getPos(), this.priceInput.getCoinValue().copy(), duration));
		
	}

	@Override
	public FontRenderer getFont() {
		return this.font;
	}
	
}
