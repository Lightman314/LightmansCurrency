package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.UniversalTradeItemPriceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.UniversalTraderNameScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.UniversalItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearUniversalLogger;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageToggleCreative;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageAddOrRemoveAlly2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenTrades2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSetTradeItem2;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class UniversalItemTraderStorageScreen extends ContainerScreen<UniversalItemTraderStorageContainer>{

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
	Button buttonClearLog;
	
	TextLogWindow logWindow;
	
	Button buttonTradeRules;
	
	boolean allyScreenOpen = false;
	Button buttonAllies;
	Button buttonAddAlly;
	Button buttonRemoveAlly;
	TextFieldWidget allyTextInput;
	
	List<Button> tradePriceButtons = new ArrayList<>();
	
	public UniversalItemTraderStorageScreen(UniversalItemTraderStorageContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		int tradeCount = this.container.getData().getTradeCount();
		this.ySize = 18 * ItemTraderStorageUtil.getRowCount(tradeCount) + 125;
		this.xSize = ItemTraderStorageUtil.getWidth(tradeCount);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		ItemTraderStorageScreen.drawTraderStorageBackground(matrix, this, this.font, this.container, this.container.getData(), this.minecraft, this.xSize, this.ySize);
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		ItemTraderStorageScreen.drawTraderStorageForeground(matrix, this.font, this.container.getData().getTradeCount(), this.ySize, this.container.getData().getName(), this.playerInventory.getDisplayName());
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		this.buttonShowTrades = this.addButton(new IconButton(this.guiLeft + SCREEN_EXTENSION, this.guiTop - 20, this::PressTradesButton, GUI_TEXTURE, 176, 0));
		
		this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft + SCREEN_EXTENSION + 20, this.guiTop - 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = !this.container.getData().isCreative() && this.container.isOwner();
		
		int tradeCount = this.container.getData().getTradeCount();
		this.buttonStoreMoney = this.addButton(new IconButton(this.guiLeft + SCREEN_EXTENSION + ItemTraderStorageUtil.getInventoryOffset(tradeCount) + 176 + 32, this.guiTop + 25 + ItemTraderStorageUtil.getRowCount(tradeCount) * 18, this::PressStoreCoinsButton, GUI_TEXTURE, 176, 16));
		this.buttonStoreMoney.visible = false;
		
		this.buttonChangeName = this.addButton(new Button(this.guiLeft + SCREEN_EXTENSION + 40, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.changename"), this::PressTraderNameButton));
		this.buttonChangeName.visible = this.container.isOwner();
		this.buttonShowLog = this.addButton(new Button(this.guiLeft + SCREEN_EXTENSION + 60, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.showlog"), this::PressLogButton));
		this.buttonClearLog = this.addButton(new Button(this.guiLeft + SCREEN_EXTENSION + 80, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.clearlog"), this::PressClearLogButton));
		this.buttonClearLog.visible = this.container.getData().getLogger().logText.size() > 0 && this.container.isOwner();
		
		this.buttonToggleCreative = this.addButton(new IconButton(this.guiLeft + this.xSize - SCREEN_EXTENSION - 40, this.guiTop - 20, this::PressCreativeButton, GUI_TEXTURE, 176 + 32, 0));
		this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.container.player);
		this.buttonAddTrade = this.addButton(new PlainButton(this.guiLeft + this.xSize - SCREEN_EXTENSION - 50, this.guiTop - 20, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 0));
		this.buttonAddTrade.visible = this.container.getData().isCreative() && TradingOffice.isAdminPlayer(this.container.player);;
		this.buttonAddTrade.active = this.container.getData().getTradeCount() < ItemTraderTileEntity.TRADELIMIT;
		this.buttonRemoveTrade = this.addButton(new PlainButton(this.guiLeft + this.xSize - SCREEN_EXTENSION - 50, this.guiTop - 10, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 20));
		this.buttonRemoveTrade.visible = this.container.getData().isCreative() && TradingOffice.isAdminPlayer(this.container.player);;
		this.buttonRemoveTrade.active = this.container.getData().getTradeCount() > 1;
		
		if(this.container.isOwner())
		{
			
			this.buttonAllies = this.addButton(new IconButton(this.guiLeft + SCREEN_EXTENSION + 100, this.guiTop - 20, this::PressAllyButton, GUI_TEXTURE, 176 + 32, 16));
			
			this.allyTextInput = this.addListener(new TextFieldWidget(this.font, this.guiLeft + this.xSize/2 - 176/2 + 10, this.guiTop + 9, 176 - 20, 20, new StringTextComponent("")));
			this.allyTextInput.setMaxStringLength(32);
			this.allyTextInput.visible = false;
			
			this.buttonAddAlly = this.addButton(new Button(this.guiLeft + this.xSize/2 - 176/2 + 10, this.guiTop + 30, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.allies.add"), this::PressAddAllyButton));
			this.buttonAddAlly.visible = false;
			this.buttonRemoveAlly = this.addButton(new Button(this.guiLeft + this.xSize/2 - 176/2 + 88, this.guiTop + 30, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.allies.remove"), this::PressRemoveAllyButton));
			this.buttonRemoveAlly.visible = false;
			
		}
		
		this.buttonTradeRules = this.addButton(new IconButton(this.guiLeft + this.xSize - SCREEN_EXTENSION - 20, this.guiTop - 20, this::PressTradeRulesButton, GUI_TEXTURE, 176 + 16, 16));
		
		this.logWindow = new TextLogWindow(this.guiLeft + (this.xSize / 2) - (TextLogWindow.WIDTH / 2), this.guiTop, () -> this.container.getData().getLogger(), this.font);
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
		else if(this.allyScreenOpen)
		{
			
			ItemTraderStorageScreen.drawAllyScreen(matrixStack, this, this.font, this.container.getData(), minecraft, this.xSize, this.ySize);
			
			this.allyTextInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonAddAlly.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonRemoveAlly.render(matrixStack, mouseX, mouseY, partialTicks);
			
			if(this.buttonAllies != null && this.buttonAllies.isMouseOver(mouseX, mouseY))
			{
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.allies"), mouseX, mouseY);
			}
			this.buttonAllies.render(matrixStack, mouseX, mouseY, partialTicks);
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
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.collectcoins", this.container.getData().getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonStoreMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.storecoins"), mouseX, mouseY);
		}
		else if(this.buttonToggleCreative.visible && this.buttonToggleCreative.isMouseOver(mouseX, mouseY))
		{
			if(this.container.getData().isCreative())
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
		else if(this.buttonAllies != null && this.buttonAllies.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.allies"), mouseX, mouseY);
		}
		else if(this.container.player.inventory.getItemStack().isEmpty())
		{
			int tradeCount = this.container.getData().getTradeCount();
			for(int i = 0; i < this.container.getData().getTradeCount(); i++)
			{
				boolean inverted = ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i);
				int result = ItemTradeButton.tryRenderTooltip(matrixStack, this, i, this.container.getData(), this.guiLeft + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.guiTop + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), inverted, mouseX, mouseY, null);
				if(result < 0) //Result is negative if the mouse is over a slot, but the slot is empty.
					this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.item_edit"), mouseX, mouseY);
			}
		}
		
	}
	
	@Override
	public void tick()
	{
		super.tick();
		this.container.tick();
		
		this.buttonCollectMoney.visible = (!this.container.getData().isCreative() || this.container.getData().getStoredMoney().getRawValue() > 0) && this.container.isOwner();
		this.buttonCollectMoney.active = this.container.getData().getStoredMoney().getRawValue() > 0;
		
		this.buttonStoreMoney.visible = this.container.HasCoinsToAdd();
		this.buttonClearLog.visible = this.container.getData().getLogger().logText.size() > 0 && this.container.isOwner();
		
		if(this.container.isOwner())
		{
			this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.container.player);;
			if(this.buttonToggleCreative.visible)
			{
				if(this.container.getData().isCreative())
				{
					this.buttonToggleCreative.setResource(GUI_TEXTURE, 176 + 32, 0);
					this.buttonAddTrade.visible = true;
					this.buttonAddTrade.active = this.container.getData().getTradeCount() < ItemTraderTileEntity.TRADELIMIT;
					this.buttonRemoveTrade.visible = true;
					this.buttonRemoveTrade.active = this.container.getData().getTradeCount() > 1;
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
			
			this.buttonAddAlly.visible = this.allyScreenOpen;
			this.buttonRemoveAlly.visible = this.allyScreenOpen;
			this.allyTextInput.visible = this.allyScreenOpen;
			
		}
		
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
		if(this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey) && this.allyScreenOpen)
			return false;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	//0 for left-click. 1 for right-click
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		ItemStack heldItem = this.container.player.inventory.getItemStack();
		int tradeCount = this.container.getData().getTradeCount();
		for(int i = 0; i < tradeCount; ++i)
		{
			ItemTradeData trade = this.container.getData().getTrade(i);
			if(ItemTradeButton.isMouseOverSlot(0, this.guiLeft + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.guiTop + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), (int)mouseX, (int)mouseY, ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i)))
			{
				ItemStack currentSellItem = trade.getSellItem();
				LightmansCurrency.LogInfo("Clicked on sell item. Click Type: " + button + "; Sell Item: " + currentSellItem.getCount() + "x" + currentSellItem.getItem().getRegistryName() + "; Held Item: " + heldItem.getCount() + "x" + heldItem.getItem().getRegistryName());
				if(heldItem.isEmpty() && currentSellItem.isEmpty())
				{
					//Open the item edit screen if both the sell item and held items are empty
					this.container.openItemEditScreenForTrade(i);
					return true;
				}
				else if(heldItem.isEmpty())
				{
					//If held item is empty, right-click to decrease by 1, left-click to empty
					currentSellItem.shrink(button == 0 ? currentSellItem.getCount() : 1);
					if(currentSellItem.getCount() <= 0)
						currentSellItem = ItemStack.EMPTY;
					trade.setSellItem(currentSellItem);
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem2(this.container.traderID, i, currentSellItem, 0));
					return true;
				}
				else
				{
					//If the held item is empty, right-click to increase by 1, left click to set to current held count
					if(button == 1)
					{
						if(InventoryUtil.ItemMatches(currentSellItem, heldItem))
						{
							if(currentSellItem.getCount() < currentSellItem.getMaxStackSize())
								currentSellItem.grow(1);
						}
						else
						{
							currentSellItem = heldItem.copy();
							currentSellItem.setCount(1);
						}
					}
					else
						currentSellItem = heldItem.copy();
					trade.setSellItem(currentSellItem);
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem2(this.container.traderID, i, currentSellItem, 0));
				}
			}
			else if(this.container.getData().getTrade(i).isBarter() && ItemTradeButton.isMouseOverSlot(1, this.guiLeft + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.guiTop + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), (int)mouseX, (int)mouseY, ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i)))
			{
				ItemStack currentBarterItem = trade.getBarterItem();
				LightmansCurrency.LogInfo("Clicked on barter item. Click Type: " + button + "; Barter Item: " + currentBarterItem.getCount() + "x" + currentBarterItem.getItem().getRegistryName() + "; Held Item: " + heldItem.getCount() + "x" + heldItem.getItem().getRegistryName());
				if(heldItem.isEmpty() && currentBarterItem.isEmpty())
				{
					//Open the item edit screen if both the sell item and held items are empty
					this.container.openItemEditScreenForTrade(i);
					return true;
				}
				else if(heldItem.isEmpty())
				{
					//If held item is empty, right-click to decrease by 1, left-click to empty
					currentBarterItem.shrink(button == 0 ? currentBarterItem.getCount() : 1);
					if(currentBarterItem.getCount() <= 0)
						currentBarterItem = ItemStack.EMPTY;
					trade.setBarterItem(currentBarterItem);
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem2(this.container.traderID, i, currentBarterItem, 1));
					return true;
				}
				else
				{
					//If the held item is empty, right-click to increase by 1, left click to set to current held count
					if(button == 1)
					{
						if(InventoryUtil.ItemMatches(currentBarterItem, heldItem))
						{
							if(currentBarterItem.getCount() < currentBarterItem.getMaxStackSize())
								currentBarterItem.grow(1);
						}
						else
						{
							currentBarterItem = heldItem.copy();
							currentBarterItem.setCount(1);
						}
					}
					else
						currentBarterItem = heldItem.copy();
					trade.setBarterItem(currentBarterItem);
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem2(this.container.traderID, i, currentBarterItem, 1));
				}
			}
		}
		//LightmansCurrency.LogInfo("Did not click on any trade definition slots.");
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	private void PressTradesButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades2(this.container.getData().getTraderID()));
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
		
		this.minecraft.displayGuiScreen(new UniversalTradeItemPriceScreen(this.container.traderID, this.container.getData().getTrade(tradeIndex), tradeIndex, this.playerInventory.player));
		
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
	
	private void PressTraderNameButton(Button button)
	{
		this.minecraft.displayGuiScreen(new UniversalTraderNameScreen(this.container.getData(), this.playerInventory.player));
	}
	
	private void PressLogButton(Button button)
	{
		this.logWindow.visible = !this.logWindow.visible;
	}
	
	private void PressClearLogButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearUniversalLogger(this.container.traderID));
	}
	
	private void PressTradeRulesButton(Button button)
	{
		Minecraft.getInstance().displayGuiScreen(new TradeRuleScreen(this.container.getData().GetRuleScreenHandler()));
	}
	
	private void PressAllyButton(Button button)
	{
		this.allyScreenOpen = !this.allyScreenOpen;
	}
	
	private void PressAddAllyButton(Button button)
	{
		String newAlly = this.allyTextInput.getText();
		this.allyTextInput.setText("");
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveAlly2(this.container.traderID, true, newAlly));
	}
	
	private void PressRemoveAllyButton(Button button)
	{
		String newAlly = this.allyTextInput.getText();
		this.allyTextInput.setText("");
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveAlly2(this.container.traderID, false, newAlly));
	}
	
}
