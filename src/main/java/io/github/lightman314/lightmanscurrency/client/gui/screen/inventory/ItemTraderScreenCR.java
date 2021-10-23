package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.*;
import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainerCR;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRNextTrader;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRSkipTo;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemTraderScreenCR extends ContainerScreen<ItemTraderContainerCR>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader.png");
	
	public static final int TRADEBUTTON_SPACER = ItemTraderScreen.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = ItemTradeButton.HEIGHT + TRADEBUTTON_SPACER;
	public static final int TRADEBUTTON_HORIZ_SPACER = 6;
	public static final int TRADEBUTTON_HORIZONTAL = ItemTradeButton.WIDTH + TRADEBUTTON_HORIZ_SPACER;
	
	Button buttonCollectMoney;
	
	Button buttonLeft;
	Button buttonRight;
	
	TextFieldWidget pageInput;
	Button buttonSkipToPage;
	
	protected List<ItemTradeButton> tradeButtons = new ArrayList<>();
	
	public ItemTraderScreenCR(ItemTraderContainerCR container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.ySize = 133 + ItemTraderUtil.getTradeDisplayHeight(this.container.tileEntity);
		this.xSize = ItemTraderUtil.getWidth(this.container.tileEntity);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		ItemTraderScreen.drawTraderBackground(matrix, this, this.container, this.minecraft, this.xSize, this.ySize, this.container.tileEntity);
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		ItemTraderScreen.drawTraderForeground(matrix, this.font, this.container.tileEntity, this.ySize,
				new TranslationTextComponent("gui.lightmanscurrency.trading.title", this.container.tileEntity.getName(), new TranslationTextComponent("gui.lightmanscurrency.trading.list", this.container.getThisIndex() + 1, this.container.getTotalCount())),
				this.playerInventory.getDisplayName(),
				new TranslationTextComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.container.GetCoinValue())));
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.container.tileEntity);
		
		//From the cash register, there is no acknowledged owner.
		if(this.container.cashRegister.getPairedTraderSize() > 1)
		{
			
			this.buttonLeft = this.addButton(new IconButton(this.guiLeft + tradeOffset - 20, this.guiTop, this::PressArrowButton, GUI_TEXTURE, 176, 16));
			this.buttonRight = this.addButton(new IconButton(this.guiLeft + this.xSize - tradeOffset, this.guiTop, this::PressArrowButton, GUI_TEXTURE, 176 + 16, 16));
			
			this.pageInput = new TextFieldWidget(this.font, this.guiLeft + 50, this.guiTop - 19, this.xSize - 120, 18, ITextComponent.getTextComponentOrEmpty(""));
			this.pageInput.setMaxStringLength(9);
			this.pageInput.setText(String.valueOf(this.container.getThisIndex() + 1));
			this.children.add(this.pageInput);
			
			this.buttonSkipToPage = this.addButton(new IconButton(this.guiLeft + this.xSize - 68, this.guiTop - 20, this::PressPageSkipButton, GUI_TEXTURE, 176 + 16, 16));
			this.buttonSkipToPage.active = false;
			
		}
		
		if(this.container.isOwner())
		{
			
			this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft - 20 + tradeOffset, this.guiTop + 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
			this.buttonCollectMoney.active = false;
		}
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.container.getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addButton(new ItemTradeButton(this.guiLeft + ItemTraderUtil.getButtonPosX(this.container.tileEntity, i), this.guiTop + ItemTraderUtil.getButtonPosY(this.container.tileEntity, i), this::PressTradeButton, i, this, this.font, () -> this.container.tileEntity, this.container)));
		}
	}
	
	public static int tradeWindowWidth(int tradeCount)
	{
		return 0;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		this.container.tick();
		
		if(this.buttonCollectMoney != null)
		{
			this.buttonCollectMoney.active = this.container.tileEntity.getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.container.tileEntity.isCreative();
		}
		if(this.buttonSkipToPage != null)
		{
			this.buttonSkipToPage.active = this.getPageInput() >= 0 && this.getPageInput() < this.container.getTotalCount() && this.getPageInput() != this.container.getThisIndex();
		}
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.pageInput != null)
			this.pageInput.render(matrixStack, mouseX, mouseY, partialTicks);
		
		if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.collectcoins", this.container.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(matrixStack, this, this.container.tileEntity, false, mouseX, mouseY, this.container);
		}
	}
	
	private void PressTradeButton(Button button)
	{
		
		int tradeIndex = 0;
		if(tradeButtons.contains(button))
			tradeIndex = tradeButtons.indexOf(button);
		
		//CurrencyMod.LOGGER.info("Trade Button clicked for index " + tradeIndex + ".");
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
		
	}
	
	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(container.isOwner())
		{
			//CurrencyMod.LOGGER.info("Owner attempted to collect the stored money.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
		else
			LightmansCurrency.LogWarning("Non-owner attempted to collect the stored money.");
	}
	
	private void PressArrowButton(Button button)
	{
		int direction = 1;
		if(button == this.buttonLeft)
			direction = -1;
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCRNextTrader(direction));
		
	}
	
	private void PressPageSkipButton(Button button)
	{
		int page = this.getPageInput();
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCRSkipTo(page));
	}
	
	private int getPageInput()
	{
		if(this.pageInput != null)
		{
			if(isNumeric(this.pageInput.getText()))
			{
				return Integer.parseInt(this.pageInput.getText()) - 1;
			}
			return 0;
		}
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
	
}
