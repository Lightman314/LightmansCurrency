package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.UniversalTradeItemPriceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearUniversalLogger;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenTrades2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSetTradeItem2;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.menus.ItemTraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.UniversalItemTraderStorageMenu;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class UniversalItemTraderStorageScreen extends AbstractContainerScreen<UniversalItemTraderStorageMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/traderstorage.png");
	
	public static final int SCREEN_EXTENSION = ItemTraderStorageMenu.SCREEN_EXTENSION;
	
	Button buttonShowTrades;
	Button buttonCollectMoney;
	Button buttonOpenSettings;
	
	Button buttonStoreMoney;
	
	Button buttonShowLog;
	Button buttonClearLog;
	
	TextLogWindow logWindow;
	
	Button buttonTradeRules;
	
	List<Button> tradePriceButtons = new ArrayList<>();
	
	public UniversalItemTraderStorageScreen(UniversalItemTraderStorageMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		int tradeCount = this.menu.getData().getTradeCount();
		this.imageHeight = 18 * ItemTraderStorageUtil.getRowCount(tradeCount) + 125;
		this.imageWidth = ItemTraderStorageUtil.getWidth(tradeCount);
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		ItemTraderStorageScreen.drawTraderStorageBackground(poseStack, this, this.font, this.menu, this.menu.getData(), this.minecraft, this.imageWidth, this.imageHeight);
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		
		ItemTraderStorageScreen.drawTraderStorageForeground(poseStack, this.font, this.menu.getData().getTradeCount(), this.imageHeight, this.menu.getData().getName(), this.playerInventoryTitle);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		this.buttonShowTrades = this.addRenderableWidget(new IconButton(this.leftPos + SCREEN_EXTENSION, this.topPos - 20, this::PressTradesButton, this.font, IconData.of(GUI_TEXTURE, 176, 0)));
		
		this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos + SCREEN_EXTENSION + 20, this.topPos - 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 0)));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getData().getCoreSettings().hasBankAccount();
		
		int tradeCount = this.menu.getData().getTradeCount();
		this.buttonStoreMoney = this.addRenderableWidget(new IconButton(this.leftPos + SCREEN_EXTENSION + ItemTraderStorageUtil.getInventoryOffset(tradeCount) + 176 + 32, this.topPos + 25 + ItemTraderStorageUtil.getRowCount(tradeCount) * 18, this::PressStoreCoinsButton, this.font, IconData.of(GUI_TEXTURE, 176, 16)));
		this.buttonStoreMoney.visible = false;
		
		this.buttonShowLog = this.addRenderableWidget(new Button(this.leftPos + SCREEN_EXTENSION + 40, this.topPos - 20, 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.showlog"), this::PressLogButton));
		this.buttonClearLog = this.addRenderableWidget(new Button(this.leftPos + SCREEN_EXTENSION + 60, this.topPos - 20, 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.clearlog"), this::PressClearLogButton));
		this.buttonClearLog.visible = this.menu.getData().getLogger().logText.size() > 0 && this.menu.hasPermission(Permissions.CLEAR_LOGS);
		
		this.buttonOpenSettings = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth - SCREEN_EXTENSION - 20, this.topPos - 20, this::PressSettingsButton, this.font, IconData.of(GUI_TEXTURE, 176 + 32, 0)));
		this.buttonOpenSettings.visible = this.menu.hasPermission(Permissions.EDIT_SETTINGS);
		
		this.buttonTradeRules = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth - SCREEN_EXTENSION - 40, this.topPos - 20, this::PressTradeRulesButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 16)));
		this.buttonTradeRules.visible = this.menu.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.logWindow = this.addWidget(new TextLogWindow(this.leftPos + (this.imageWidth / 2) - (TextLogWindow.WIDTH / 2), this.topPos, () -> this.menu.getData().getLogger(), this.font));
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
			if(this.buttonClearLog.visible)
				this.buttonClearLog.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonShowLog.isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.hide"), mouseX, mouseY);
			else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
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
		else if(this.buttonShowLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.show"), mouseX, mouseY);
		}
		else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
		}
		else if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		}
		else if(this.buttonOpenSettings.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.settings"), mouseX, mouseY);
		}
		else if(this.menu.getCarried().isEmpty())
		{
			int tradeCount = this.menu.getData().getTradeCount();
			for(int i = 0; i < this.menu.getData().getTradeCount(); i++)
			{
				boolean inverted = ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i);
				int result = ItemTradeButton.tryRenderTooltip(matrixStack, this, i, this.menu.getData(), this.leftPos + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.topPos + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), inverted, mouseX, mouseY);
				if(result < 0 && this.menu.hasPermission(Permissions.EDIT_TRADES)) //Result is negative if the mouse is over a slot, but the slot is empty.
					this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.item_edit"), mouseX, mouseY);
			}
		}
		
	}
	
	@Override
	public void containerTick()
	{

		this.menu.tick();
		
		this.buttonCollectMoney.visible = (!this.menu.getData().getCoreSettings().isCreative() || this.menu.getData().getStoredMoney().getRawValue() > 0) && this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getData().getCoreSettings().hasBankAccount();
		this.buttonCollectMoney.active = this.menu.getData().getStoredMoney().getRawValue() > 0;
		
		this.buttonOpenSettings.visible = this.menu.hasPermission(Permissions.EDIT_SETTINGS);
		
		this.buttonStoreMoney.visible = this.menu.HasCoinsToAdd() && this.menu.hasPermission(Permissions.STORE_COINS);
		this.buttonClearLog.visible = this.menu.getData().getLogger().logText.size() > 0 && this.menu.hasPermission(Permissions.CLEAR_LOGS);
		
		this.buttonTradeRules.visible = this.menu.hasPermission(Permissions.EDIT_TRADE_RULES);
		
	}
	
	//0 for left-click. 1 for right-click
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
		{
			return super.mouseClicked(mouseX, mouseY, button);
		}
		ItemStack heldItem = this.menu.getCarried();
		int tradeCount = this.menu.getData().getTradeCount();
		for(int i = 0; i < tradeCount; ++i)
		{
			ItemTradeData trade = this.menu.getData().getTrade(i);
			if(ItemTradeButton.isMouseOverSlot(0, this.leftPos + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.topPos + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), (int)mouseX, (int)mouseY, ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i)))
			{
				ItemStack currentSellItem = trade.getSellItem();
				LightmansCurrency.LogInfo("Clicked on sell item. Click Type: " + button + "; Sell Item: " + currentSellItem.getCount() + "x" + currentSellItem.getItem().getRegistryName() + "; Held Item: " + heldItem.getCount() + "x" + heldItem.getItem().getRegistryName());
				if(heldItem.isEmpty() && currentSellItem.isEmpty())
				{
					//Open the item edit screen if both the sell item and held items are empty
					this.menu.openItemEditScreenForTrade(i);
					return true;
				}
				else if(heldItem.isEmpty())
				{
					//If held item is empty, right-click to decrease by 1, left-click to empty
					currentSellItem.shrink(button == 0 ? currentSellItem.getCount() : 1);
					if(currentSellItem.getCount() <= 0)
						currentSellItem = ItemStack.EMPTY;
					trade.setSellItem(currentSellItem);
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem2(this.menu.traderID, i, currentSellItem, 0));
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
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem2(this.menu.traderID, i, currentSellItem, 0));
				}
			}
			else if(this.menu.getData().getTrade(i).isBarter() && ItemTradeButton.isMouseOverSlot(1, this.leftPos + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.topPos + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), (int)mouseX, (int)mouseY, ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i)))
			{
				ItemStack currentBarterItem = trade.getBarterItem();
				LightmansCurrency.LogInfo("Clicked on barter item. Click Type: " + button + "; Barter Item: " + currentBarterItem.getCount() + "x" + currentBarterItem.getItem().getRegistryName() + "; Held Item: " + heldItem.getCount() + "x" + heldItem.getItem().getRegistryName());
				if(heldItem.isEmpty() && currentBarterItem.isEmpty())
				{
					//Open the item edit screen if both the sell item and held items are empty
					this.menu.openItemEditScreenForTrade(i);
					return true;
				}
				else if(heldItem.isEmpty())
				{
					//If held item is empty, right-click to decrease by 1, left-click to empty
					currentBarterItem.shrink(button == 0 ? currentBarterItem.getCount() : 1);
					if(currentBarterItem.getCount() <= 0)
						currentBarterItem = ItemStack.EMPTY;
					trade.setBarterItem(currentBarterItem);
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem2(this.menu.traderID, i, currentBarterItem, 1));
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
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem2(this.menu.traderID, i, currentBarterItem, 1));
				}
			}
		}
		//LightmansCurrency.LogInfo("Did not click on any trade definition slots.");
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	private void PressTradesButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades2(this.menu.getData().getTraderID()));
	}
	
	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			//CurrencyMod.LOGGER.info("Owner attempted to collect the stored money.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
	}
	
	private void PressStoreCoinsButton(Button button)
	{
		if(this.menu.hasPermission(Permissions.STORE_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStoreCoins());
		}
	}
	
	private void PressTradePriceButton(Button button)
	{
		if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
			return;
		int tradeIndex = 0;
		if(tradePriceButtons.contains(button))
			tradeIndex = tradePriceButtons.indexOf(button);
		
		this.minecraft.setScreen(new UniversalTradeItemPriceScreen(this.menu.traderID, this.menu.getData().getTrade(tradeIndex), tradeIndex, this.menu.player));
		
	}
	
	private void PressLogButton(Button button)
	{
		this.logWindow.visible = !this.logWindow.visible;
	}
	
	private void PressClearLogButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearUniversalLogger(this.menu.traderID));
	}
	
	private void PressTradeRulesButton(Button button)
	{
		Minecraft.getInstance().setScreen(new TradeRuleScreen(this.menu.getData().GetRuleScreenHandler()));
	}
	
	private void PressSettingsButton(Button button)
	{
		this.menu.player.closeContainer();
		Minecraft.getInstance().setScreen(new TraderSettingsScreen(() -> ClientTradingOffice.getData(this.menu.traderID), (player) -> LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(this.menu.traderID))));
	}
	
}
