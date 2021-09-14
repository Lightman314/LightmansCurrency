package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.UniversalTradeItemPriceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.UniversalTraderNameScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.UniversalItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.TradeInputSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageToggleCreative;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenTrades2;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;

public class UniversalItemTraderStorageScreen extends AbstractContainerScreen<UniversalItemTraderStorageContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/traderstorage.png");
	
	public static final int SCREEN_EXTENSION = ItemTraderStorageContainer.SCREEN_EXTENSION;
	
	Button buttonShowTrades;
	Button buttonCollectMoney;
	Button buttonStoreMoney;
	IconButton buttonToggleCreative;
	Button buttonAddTrade;
	Button buttonRemoveTrade;
	
	Button buttonChangeName;
	
	Button buttonShowLog;
	
	TextLogWindow logWindow;
	
	List<Button> tradePriceButtons = new ArrayList<>();
	
	public UniversalItemTraderStorageScreen(UniversalItemTraderStorageContainer container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		int tradeCount = this.menu.getData().getTradeCount();
		this.imageHeight = 18 * ItemTraderStorageUtil.getRowCount(tradeCount) + 125;
		this.imageWidth = ItemTraderStorageUtil.getWidth(tradeCount);
	}
	
	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		ItemTraderStorageScreen.drawTraderStorageBackground(matrix, this, this.menu, this.menu.getData().getAllTrades(), this.minecraft, this.imageWidth, this.imageHeight);
	}
	
	@Override
	protected void renderLabels(PoseStack matrix, int mouseX, int mouseY)
	{
		ItemTraderStorageScreen.drawTraderStorageForeground(matrix, this.font, this.menu.getData().getTradeCount(), this.imageHeight, this.menu.getData().getName(), this.playerInventoryTitle, this.menu.getData().getAllTrades());
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		this.buttonShowTrades = this.addRenderableWidget(new IconButton(this.leftPos + SCREEN_EXTENSION, this.topPos - 20, this::PressTradesButton, GUI_TEXTURE, 176, 0));
		
		this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos + SCREEN_EXTENSION + 20, this.topPos - 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
		this.buttonCollectMoney.active = false;
		
		int tradeCount = this.menu.getData().getTradeCount();
		this.buttonStoreMoney = this.addRenderableWidget(new IconButton(this.leftPos + SCREEN_EXTENSION + ItemTraderStorageUtil.getInventoryOffset(tradeCount) + 176 + 32, this.topPos + 25 + ItemTraderStorageUtil.getRowCount(tradeCount) * 18, this::PressStoreCoinsButton, GUI_TEXTURE, 176, 16));
		this.buttonStoreMoney.visible = false;
		
		this.buttonChangeName = this.addRenderableWidget(new Button(this.leftPos + SCREEN_EXTENSION + 40, this.topPos - 20, 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.changename"), this::PressTraderNameButton));
		this.buttonShowLog = this.addRenderableWidget(new Button(this.leftPos + SCREEN_EXTENSION + 60, this.topPos - 20, 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.showlog"), this::PressLogButton));
		
		this.buttonToggleCreative = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth - SCREEN_EXTENSION - 20, this.topPos - 20, this::PressCreativeButton, GUI_TEXTURE, 176 + 32, 0));
		this.buttonToggleCreative.visible = this.menu.player.isCreative() && this.menu.player.hasPermissions(2);
		this.buttonAddTrade = this.addRenderableWidget(new PlainButton(this.leftPos + this.imageWidth - SCREEN_EXTENSION - 30, this.topPos - 20, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 0));
		this.buttonAddTrade.visible = this.menu.getData().isCreative();
		this.buttonAddTrade.active = this.menu.getData().getTradeCount() < ItemTraderBlockEntity.TRADELIMIT;
		this.buttonRemoveTrade = this.addRenderableWidget(new PlainButton(this.leftPos + this.imageWidth - SCREEN_EXTENSION - 30, this.topPos - 10, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 20));
		this.buttonRemoveTrade.visible = this.menu.getData().isCreative();
		this.buttonRemoveTrade.active = this.menu.getData().getTradeCount() > 1;
		
		this.logWindow = this.addWidget(new TextLogWindow(this.leftPos + (this.imageWidth / 2) - (TextLogWindow.WIDTH / 2), this.topPos, () -> this.menu.getData().logger, this.font));
		this.logWindow.visible = false;
		
		for(int i = 0; i < tradeCount; i++)
		{
			tradePriceButtons.add(this.addRenderableWidget(new Button(this.leftPos + ItemTraderStorageUtil.getTradePriceButtonPosX(tradeCount, i), this.topPos + ItemTraderStorageUtil.getTradePriceButtonPosY(tradeCount, i), 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.dollarsign"), this::PressTradePriceButton)));
		}
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		if(this.logWindow.visible)
		{
			this.logWindow.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonShowLog.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonShowLog.isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.hide"), mouseX, mouseY);
			return;
		}
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonShowTrades.isMouseOver(mouseX,mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.opentrades"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.getData().getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonStoreMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.storecoins"), mouseX, mouseY);
		}
		else if(this.buttonToggleCreative.visible && this.buttonToggleCreative.isMouseOver(mouseX, mouseY))
		{
			if(this.menu.getData().isCreative())
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.disable"), mouseX, mouseY);
			else
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.enable"), mouseX, mouseY);
		}
		else if(this.buttonAddTrade.visible && this.buttonAddTrade.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.addTrade"), mouseX, mouseY);
		}
		else if(this.buttonRemoveTrade.visible && this.buttonRemoveTrade.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.removeTrade"), mouseX, mouseY);
		}
		else if(this.buttonChangeName.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.changeName"), mouseX, mouseY);
		}
		else if(this.buttonShowLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.show"), mouseX, mouseY);
		}
		else if(this.menu.getCarried().isEmpty())
		{
			this.menu.slots.forEach(slot ->{
				if(slot instanceof TradeInputSlot && !slot.hasItem())
				{
					TradeInputSlot inputSlot = (TradeInputSlot)slot;
					if(inputSlot.isMouseOver(mouseX, mouseY, this.leftPos, this.topPos))
					{
						this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.item_edit"), mouseX, mouseY);
					}
				}
			});
		}
		
	}
	
	@Override
	public void containerTick()
	{
		this.menu.tick();
		
		this.buttonCollectMoney.visible = !this.menu.getData().isCreative() || this.menu.getData().getStoredMoney().getRawValue() > 0;
		this.buttonCollectMoney.active = this.menu.getData().getStoredMoney().getRawValue() > 0;
		
		this.buttonStoreMoney.visible = this.menu.HasCoinsToAdd();
		
		this.buttonToggleCreative.visible = this.menu.player.isCreative() && this.menu.player.hasPermissions(2);
		if(this.buttonToggleCreative.visible)
		{
			if(this.menu.getData().isCreative())
			{
				this.buttonToggleCreative.setResource(GUI_TEXTURE, 176 + 32, 0);
				this.buttonAddTrade.visible = true;
				this.buttonAddTrade.active = this.menu.getData().getTradeCount() < ItemTraderBlockEntity.TRADELIMIT;
				this.buttonRemoveTrade.visible = true;
				this.buttonRemoveTrade.active = this.menu.getData().getTradeCount() > 1;
			}
			else
			{
				this.buttonToggleCreative.setResource(GUI_TEXTURE, 176 + 48, 0);
				this.buttonAddTrade.visible = false;
				this.buttonRemoveTrade.visible = false;
			}
		}
		else
		{
			this.buttonAddTrade.visible = false;
			this.buttonRemoveTrade.visible = false;
		}
		
	}
	
	private void PressTradesButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades2(this.menu.getData().getTraderID()));
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
	
	private void PressStoreCoinsButton(Button button)
	{
		if(menu.isOwner())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStoreCoins());
		}
		else
			LightmansCurrency.LogWarning("Non-owner attempted to store coins in the machine.");
	}
	
	private void PressTradePriceButton(Button button)
	{
		int tradeIndex = 0;
		if(tradePriceButtons.contains(button))
			tradeIndex = tradePriceButtons.indexOf(button);
		
		this.minecraft.setScreen(new UniversalTradeItemPriceScreen(this.menu.traderID, this.menu.getData().getTrade(tradeIndex), tradeIndex));
		
	}
	
	private void PressCreativeButton(Button button)
	{
		if(menu.isOwner())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageToggleCreative());
		}
	}
	
	private void PressAddRemoveTradeButton(Button button)
	{
		if(menu.isOwner())
		{
			if(button == this.buttonAddTrade)
			{
				LightmansCurrency.LogInfo("Add Trade Button Pressed!");
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(true));
			}
			else
			{
				LightmansCurrency.LogInfo("Remove Trade Button Pressed!");
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(false));
			}
		}
	}
	
	private void PressTraderNameButton(Button button)
	{
		this.minecraft.setScreen(new UniversalTraderNameScreen(this.menu.getData(), this.menu.player));
	}
	
	private void PressLogButton(Button button)
	{
		this.logWindow.visible = !this.logWindow.visible;
	}
	
}
