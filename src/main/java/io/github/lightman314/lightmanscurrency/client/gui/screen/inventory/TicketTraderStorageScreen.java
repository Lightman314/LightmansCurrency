package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeTicketPriceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderNameScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.containers.TicketTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.TradeInputSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearLogger;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenTrades;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageToggleCreative;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.tradedata.TicketTradeData;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TicketTraderStorageScreen extends ContainerScreen<TicketTraderStorageContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/traderstorage.png");
	
	public static final int SCREEN_EXTENSION = ItemTraderStorageUtil.SCREEN_EXTENSION;
	
	Button buttonShowTrades;
	Button buttonCollectMoney;
	Button buttonStoreMoney;
	IconButton buttonToggleCreative;
	Button buttonAddTrade;
	Button buttonRemoveTrade;
	
	Button buttonChangeName;
	
	Button buttonShowLog;
	Button buttonClearLog;
	
	TextLogWindow logWindow;
	
	Button buttonTradeRules;
	
	List<Button> tradePriceButtons = new ArrayList<>();
	
	public TicketTraderStorageScreen(TicketTraderStorageContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		int tradeCount = this.container.tileEntity.getTradeCount();
		this.ySize = 18 * ItemTraderStorageUtil.getRowCount(tradeCount) + 125;
		this.xSize = ItemTraderStorageUtil.getWidth(tradeCount);
		
	}
	
	private NonNullList<ItemTradeData> getEmulatedTrades()
	{
		NonNullList<TicketTradeData> list = this.container.tileEntity.getAllTrades();
		NonNullList<ItemTradeData> emulatedList = NonNullList.withSize(list.size(), new ItemTradeData());
		for(int i = 0; i < list.size(); i++)
			emulatedList.set(i, list.get(i).emulateItemTrade());
		return emulatedList;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		ItemTraderStorageScreen.drawTraderStorageBackground(matrix, this, this.container, this.getEmulatedTrades(), this.minecraft, this.xSize, this.ySize);
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		ItemTraderStorageScreen.drawTraderStorageForeground(matrix, this.font, this.container.tileEntity.getTradeCount(), this.ySize, this.container.tileEntity.getName(), this.playerInventory.getDisplayName(), this.getEmulatedTrades());
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		this.buttonShowTrades = this.addButton(new IconButton(this.guiLeft + SCREEN_EXTENSION, this.guiTop - 20, this::PressTradesButton, GUI_TEXTURE, 176, 0));
		
		this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft + SCREEN_EXTENSION + 20, this.guiTop - 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = !this.container.tileEntity.isCreative();
		
		int tradeCount = this.container.tileEntity.getTradeCount();
		this.buttonStoreMoney = this.addButton(new IconButton(this.guiLeft + SCREEN_EXTENSION + ItemTraderStorageUtil.getInventoryOffset(tradeCount) + 176 + 32, this.guiTop + 25 + ItemTraderStorageUtil.getRowCount(tradeCount) * 18, this::PressStoreCoinsButton, GUI_TEXTURE, 176, 16));
		this.buttonStoreMoney.visible = false;
		
		this.buttonChangeName = this.addButton(new Button(this.guiLeft + SCREEN_EXTENSION + 40, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.changename"), this::PressTraderNameButton));
		this.buttonShowLog = this.addButton(new Button(this.guiLeft + SCREEN_EXTENSION + 60, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.showlog"), this::PressLogButton));
		this.buttonClearLog = this.addButton(new Button(this.guiLeft + SCREEN_EXTENSION + 80, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.clearlog"), this::PressClearLogButton));
		
		this.buttonToggleCreative = this.addButton(new IconButton(this.guiLeft + this.xSize - SCREEN_EXTENSION - 40, this.guiTop - 20, this::PressCreativeButton, GUI_TEXTURE, 176 + 32, 0));
		this.buttonToggleCreative.visible = this.playerInventory.player.isCreative() && this.playerInventory.player.hasPermissionLevel(2);
		this.buttonAddTrade = this.addButton(new PlainButton(this.guiLeft + this.xSize - SCREEN_EXTENSION - 50, this.guiTop - 20, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 0));
		this.buttonAddTrade.visible = this.container.tileEntity.isCreative();
		this.buttonAddTrade.active = this.container.tileEntity.getTradeCount() < ItemTraderTileEntity.TRADELIMIT;
		this.buttonRemoveTrade = this.addButton(new PlainButton(this.guiLeft + this.xSize - SCREEN_EXTENSION - 50, this.guiTop - 10, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 20));
		this.buttonRemoveTrade.visible = this.container.tileEntity.isCreative();
		this.buttonRemoveTrade.active = this.container.tileEntity.getTradeCount() > 1;
		
		this.buttonTradeRules = this.addButton(new IconButton(this.guiLeft + this.xSize - SCREEN_EXTENSION - 20, this.guiTop - 20, this::PressTradeRulesButton, GUI_TEXTURE, 176 + 16, 16));
		
		this.logWindow = new TextLogWindow(this.guiLeft + (this.xSize / 2) - (TextLogWindow.WIDTH / 2), this.guiTop, () -> this.container.tileEntity.getLogger(), this.font);
		this.addListener(this.logWindow);
		this.logWindow.visible = false;
		
		for(int i = 0; i < tradeCount; i++)
		{
			tradePriceButtons.add(this.addButton(new Button(this.guiLeft + ItemTraderStorageUtil.getTradePriceButtonPosX(tradeCount, i), this.guiTop + ItemTraderStorageUtil.getTradePriceButtonPosY(tradeCount, i), 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.dollarsign"), this::PressTradePriceButton)));
		}
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		if(this.logWindow.visible)
		{
			this.logWindow.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonShowLog.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonClearLog.visible)
				this.buttonClearLog.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonShowLog.isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.hide"), mouseX, mouseY);
			else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
			return;
		}
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonShowTrades.isMouseOver(mouseX,mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.opentrades"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.collectcoins", this.container.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonStoreMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.storecoins"), mouseX, mouseY);
		}
		else if(this.buttonToggleCreative.visible && this.buttonToggleCreative.isMouseOver(mouseX, mouseY))
		{
			if(this.container.tileEntity.isCreative())
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.disable"), mouseX, mouseY);
			else
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.enable"), mouseX, mouseY);
		}
		else if(this.buttonAddTrade.visible && this.buttonAddTrade.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.addTrade"), mouseX, mouseY);
		}
		else if(this.buttonRemoveTrade.visible && this.buttonRemoveTrade.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.removeTrade"), mouseX, mouseY);
		}
		else if(this.buttonChangeName.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.changeName"), mouseX, mouseY);
		}
		else if(this.buttonShowLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.show"), mouseX, mouseY);
		}
		else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
		}
		else if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		}
		else if(this.container.player.inventory.getItemStack().isEmpty())
		{
			this.container.inventorySlots.forEach(slot ->{
				if(slot instanceof TradeInputSlot && slot.getStack().isEmpty())
				{
					TradeInputSlot inputSlot = (TradeInputSlot)slot;
					if(inputSlot.isMouseOver(mouseX, mouseY, this.guiLeft, this.guiTop))
					{
						this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.item_edit"), mouseX, mouseY);
					}
				}
			});
		}
		
		
		
	}
	
	@Override
	public void tick()
	{
		if(!this.container.isOwner())
		{
			this.container.player.closeScreen();
			return;
		}
		super.tick();
		
		this.container.tick();
		
		this.buttonCollectMoney.visible = !this.container.tileEntity.isCreative() || this.container.tileEntity.getStoredMoney().getRawValue() > 0;
		this.buttonCollectMoney.active = this.container.tileEntity.getStoredMoney().getRawValue() > 0;
		
		this.buttonStoreMoney.visible = this.container.HasCoinsToAdd();
		this.buttonClearLog.visible = this.container.tileEntity.getLogger().logText.size() > 0;
		
		this.buttonToggleCreative.visible = this.playerInventory.player.isCreative() && this.playerInventory.player.hasPermissionLevel(2);
		if(this.buttonToggleCreative.visible)
		{
			if(this.container.tileEntity.isCreative())
			{
				this.buttonToggleCreative.setResource(GUI_TEXTURE, 176 + 32, 0);
				this.buttonAddTrade.visible = true;
				this.buttonAddTrade.active = this.container.tileEntity.getTradeCount() < ItemTraderTileEntity.TRADELIMIT;
				this.buttonRemoveTrade.visible = true;
				this.buttonRemoveTrade.active = this.container.tileEntity.getTradeCount() > 1;
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
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.container.tileEntity.getPos()));
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
	
	private void PressStoreCoinsButton(Button button)
	{
		if(container.isOwner())
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
		
		this.minecraft.displayGuiScreen(new TradeTicketPriceScreen(this.container.tileEntity, tradeIndex, this.playerInventory.player));
		
	}
	
	private void PressTraderNameButton(Button button)
	{
		this.minecraft.displayGuiScreen(new TraderNameScreen(this.container.tileEntity, this.playerInventory.player));
	}
	
	private void PressCreativeButton(Button button)
	{
		if(container.isOwner())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageToggleCreative());
		}
	}
	
	private void PressAddRemoveTradeButton(Button button)
	{
		if(container.isOwner())
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
	
	private void PressLogButton(Button button)
	{
		this.logWindow.visible = !this.logWindow.visible;
	}
	
	private void PressClearLogButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearLogger(this.container.tileEntity.getPos()));
	}
	
	private void PressTradeRulesButton(Button button)
	{
		Minecraft.getInstance().displayGuiScreen(new TradeRuleScreen(this.container.tileEntity.GetRuleScreenBackHandler()));
	}
	
}
