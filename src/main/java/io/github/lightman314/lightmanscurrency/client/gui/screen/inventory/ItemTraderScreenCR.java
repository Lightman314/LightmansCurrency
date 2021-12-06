package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.*;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRNextTrader;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRSkipTo;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.menus.ItemTraderMenuCR;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class ItemTraderScreenCR extends AbstractContainerScreen<ItemTraderMenuCR>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader.png");
	
	public static final int TRADEBUTTON_SPACER = ItemTraderScreen.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = ItemTradeButton.HEIGHT + TRADEBUTTON_SPACER;
	public static final int TRADEBUTTON_HORIZ_SPACER = 6;
	public static final int TRADEBUTTON_HORIZONTAL = ItemTradeButton.WIDTH + TRADEBUTTON_HORIZ_SPACER;
	
	Button buttonCollectMoney;
	
	Button buttonLeft;
	Button buttonRight;
	
	EditBox pageInput;
	Button buttonSkipToPage;
	
	protected List<ItemTradeButton> tradeButtons = new ArrayList<>();
	
	public ItemTraderScreenCR(ItemTraderMenuCR container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = 133 + ItemTraderUtil.getTradeDisplayHeight(this.menu.tileEntity);
		this.imageWidth = ItemTraderUtil.getWidth(this.menu.tileEntity);
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		ItemTraderScreen.drawTraderBackground(poseStack, this, this.menu, this.minecraft, this.imageWidth, this.imageHeight, this.menu.tileEntity);
		
	}
	
	@Override
	protected void renderLabels(PoseStack matrix, int mouseX, int mouseY)
	{
		
		ItemTraderScreen.drawTraderForeground(matrix, this.font, this.menu.tileEntity, this.imageHeight,
				new TranslatableComponent("gui.lightmanscurrency.trading.title", this.menu.tileEntity.getName(), new TranslatableComponent("gui.lightmanscurrency.trading.list", this.menu.getThisIndex() + 1, this.menu.getTotalCount())),
				this.playerInventoryTitle,
				new TranslatableComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.menu.GetCoinValue())));
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.menu.tileEntity);
		
		//From the cash register, there is no acknowledged owner.
		if(this.menu.cashRegister.getPairedTraderSize() > 1)
		{
			
			this.buttonLeft = this.addRenderableWidget(new IconButton(this.leftPos + tradeOffset - 20, this.topPos, this::PressArrowButton, GUI_TEXTURE, 176, 16));
			this.buttonRight = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth - tradeOffset, this.topPos, this::PressArrowButton, GUI_TEXTURE, 176 + 16, 16));
			
			this.pageInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 50, this.topPos - 19, this.imageWidth - 120, 18, new TextComponent("")));
			this.pageInput.setMaxLength(9);
			this.pageInput.setValue(String.valueOf(this.menu.getThisIndex() + 1));
			
			this.buttonSkipToPage = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth - 68, this.topPos - 20, this::PressPageSkipButton, GUI_TEXTURE, 176 + 16, 16));
			this.buttonSkipToPage.active = false;
			
		}
		
		if(this.menu.isOwner())
		{
			
			this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos + 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
			this.buttonCollectMoney.active = false;
		}
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.menu.getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addRenderableWidget(new ItemTradeButton(this.leftPos + ItemTraderUtil.getButtonPosX(this.menu.tileEntity, i), this.topPos + ItemTraderUtil.getButtonPosY(this.menu.tileEntity, i), this::PressTradeButton, i, this, this.font, () -> this.menu.tileEntity, this.menu)));
		}
	}
	
	public static int tradeWindowWidth(int tradeCount)
	{
		return 0;
	}
	
	@Override
	public void containerTick()
	{
		
		this.menu.tick();
		
		if(this.buttonCollectMoney != null)
		{
			this.buttonCollectMoney.active = this.menu.tileEntity.getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.menu.tileEntity.isCreative();
		}
		if(this.buttonSkipToPage != null)
		{
			this.buttonSkipToPage.active = this.getPageInput() >= 0 && this.getPageInput() < this.menu.getTotalCount() && this.getPageInput() != this.menu.getThisIndex();
		}
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.pageInput != null)
			this.pageInput.render(matrixStack, mouseX, mouseY, partialTicks);
		
		if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(matrixStack, this, this.menu.tileEntity, false, mouseX, mouseY, this.menu);
		}
	}
	
	private void PressTradeButton(Button button)
	{
		
		int tradeIndex = 0;
		if(tradeButtons.contains(button))
			tradeIndex = tradeButtons.indexOf(button);
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
		
	}
	
	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(menu.isOwner())
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
			return TextInputUtil.getIntegerValue(this.pageInput);
		}
		return 0;
	}
	
}
