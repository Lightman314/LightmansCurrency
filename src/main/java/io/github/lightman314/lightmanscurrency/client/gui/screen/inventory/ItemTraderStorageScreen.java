package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeItemPriceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

@IPNIgnore
public class ItemTraderStorageScreen extends ContainerScreen<ItemTraderStorageContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/traderstorage.png");
	
	public static final int SCREEN_EXTENSION = ItemTraderStorageUtil.SCREEN_EXTENSION;
	
	Button buttonShowTrades;
	Button buttonCollectMoney;
	Button buttonOpenSettings;
	
	Button buttonStoreMoney;
	
	Button buttonShowLog;
	Button buttonClearLog;
	
	TextLogWindow logWindow;
	
	Button buttonTradeRules;
	
	List<Button> tradePriceButtons = new ArrayList<>();
	
	public ItemTraderStorageScreen(ItemTraderStorageContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		int tradeCount = this.container.getTrader().getTradeCount();
		this.ySize = 18 * ItemTraderStorageUtil.getRowCount(tradeCount) + 125;
		this.xSize = ItemTraderStorageUtil.getWidth(tradeCount);
		
	}
	
	@Override
	@SuppressWarnings("deprecation")
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		if(this.container.getTrader() == null)
			return;
		
		List<ItemTradeData> trades = this.container.getTrader().getAllTrades();
		int tradeCount = trades.size();
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - xSize) / 2;
		int startY = (this.height - ySize) / 2;
		
		int rowCount = ItemTraderStorageUtil.getRowCount(tradeCount);
		int columnCount = ItemTraderStorageUtil.getColumnCount(tradeCount);
		
		//Top-left corner
		this.blit(matrix, startX + SCREEN_EXTENSION, startY, 0, 0, 7, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Draw the top
			this.blit(matrix, startX + SCREEN_EXTENSION + 7 + (x * 162), startY, 7, 0, 162, 17);
		}
		//Top-right corner
		this.blit(matrix, startX + xSize - SCREEN_EXTENSION - 7, startY, 169, 0, 7, 17);
		
		//Draw the storage rows
		int index = 0;
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			this.blit(matrix, startX + SCREEN_EXTENSION, startY + 17 + 18 * y, 0, 17, 7, 18);
			if(y < rowCount - 1 || tradeCount % ItemTraderStorageUtil.getColumnCount(tradeCount) == 0 || y == 0)
			{
				for(int x = 0; x < columnCount; x++)
				{
					if(index < tradeCount)
						this.blit(matrix, startX + SCREEN_EXTENSION + 7 + (x * 162), startY + 17 + 18 * y, 7, 17, 162, 18);
					else
						this.blit(matrix, startX + SCREEN_EXTENSION + 7 + (x * 162), startY + 17 + 18 * y, 0, 143, 162, 18);
					index++;
				}
			}
			else //Trade Count is NOT even, and is on the last row, AND is not the first row
			{
				for(int x = 0; x < columnCount; x++)
				{
					//Draw a blank background
					this.blit(matrix, startX + SCREEN_EXTENSION + 7 + (x * 162), startY + 17 + 18 * y, 0, 143, 162, 18);
				}
				this.blit(matrix, startX + SCREEN_EXTENSION + 7 + ItemTraderStorageUtil.getStorageSlotOffset(tradeCount, y), startY + 17 + 18 * y, 7, 17, 162, 18);
			}
			//Right edge
			this.blit(matrix, startX + xSize - SCREEN_EXTENSION - 7, startY + 17 + 18 * y, 169, 17, 7, 18);
		}
		
		//Bottom-left corner
		this.blit(matrix, startX + SCREEN_EXTENSION, startY + 17 + 18 * rowCount, 0, 35, 7, 8);
		for(int x = 0; x < columnCount; x++)
		{
			//Draw the bottom
			this.blit(matrix, startX + SCREEN_EXTENSION + 7 + (x * 162), startY + 17 + 18 * rowCount, 7, 35, 162, 8);
		}
		//Bottom-right corner
		this.blit(matrix, startX + xSize - SCREEN_EXTENSION - 7, startY + 17 + 18 * rowCount, 169, 35, 7, 8);
		
		//Draw the bottom
		this.blit(matrix, startX + SCREEN_EXTENSION + ItemTraderStorageUtil.getInventoryOffset(tradeCount), startY + 25 + rowCount * 18, 0, 43, 176 + 32, 100);
		
		//Render the fake trade buttons
		for(int i = 0; i < tradeCount; i++)
		{
			boolean inverted = ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i);
			ItemTradeButton.renderItemTradeButton(matrix, this, font, startX + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), startY + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), i, this.container.getTrader(), inverted);
		}
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		if(this.container.getTrader() == null)
			return;
		
		font.drawString(matrix, this.container.getTrader().getName().getString(), 8.0f + SCREEN_EXTENSION, 6.0f, 0x404040);

		font.drawString(matrix, this.playerInventory.getName().getString(), ItemTraderStorageUtil.getInventoryOffset(this.container.getTrader().getTradeCount()) + 8.0f + SCREEN_EXTENSION, (ySize - 94), 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		this.buttonShowTrades = this.addButton(IconAndButtonUtil.traderButton(this.guiLeft + SCREEN_EXTENSION, this.guiTop - 20, this::PressTradesButton));
		
		this.buttonCollectMoney = this.addButton(IconAndButtonUtil.collectCoinButton(this.guiLeft + SCREEN_EXTENSION + 20, this.guiTop - 20, this::PressCollectionButton, this.container::getTrader));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.container.hasPermission(Permissions.COLLECT_COINS) && !this.container.getTrader().getCoreSettings().hasBankAccount();
		
		int tradeCount = this.container.getTrader().getTradeCount();
		this.buttonStoreMoney = this.addButton(IconAndButtonUtil.storeCoinButton(this.guiLeft + SCREEN_EXTENSION + ItemTraderStorageUtil.getInventoryOffset(tradeCount) + 176 + 32, this.guiTop + 25 + ItemTraderStorageUtil.getRowCount(tradeCount) * 18, this::PressStoreCoinsButton));
		this.buttonStoreMoney.visible = false;
		
		this.buttonShowLog = this.addButton(IconAndButtonUtil.showLoggerButton(this.guiLeft + SCREEN_EXTENSION + 40, this.guiTop - 20, this::PressLogButton, () -> this.logWindow.visible));
		this.buttonClearLog = this.addButton(IconAndButtonUtil.clearLoggerButton(this.guiLeft + SCREEN_EXTENSION + 60, this.guiTop - 20, this::PressClearLogButton));
		this.buttonClearLog.visible = this.container.getTrader().getLogger().logText.size() > 0 && this.container.hasPermission(Permissions.CLEAR_LOGS);
		
		this.buttonOpenSettings = this.addButton(IconAndButtonUtil.openSettingsButton(this.guiLeft + this.xSize - SCREEN_EXTENSION - 20, this.guiTop - 20, this::PressSettingsButton));
		this.buttonOpenSettings.visible = this.container.hasPermission(Permissions.EDIT_SETTINGS);
		
		this.buttonTradeRules = this.addButton(IconAndButtonUtil.tradeRuleButton(this.guiLeft + this.xSize - SCREEN_EXTENSION - 40, this.guiTop - 20, this::PressTradeRulesButton));
		this.buttonTradeRules.visible = this.container.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.logWindow = this.addListener(IconAndButtonUtil.traderLogWindow(this, this.container::getTrader));
		this.logWindow.visible = false;
		
		for(int i = 0; i < tradeCount; i++)
		{
			tradePriceButtons.add(this.addButton(new Button(this.guiLeft + ItemTraderStorageUtil.getTradePriceButtonPosX(tradeCount, i), this.guiTop + ItemTraderStorageUtil.getTradePriceButtonPosY(tradeCount, i), 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.dollarsign"), this::PressTradePriceButton)));
		}
		
		tick();
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		if(this.container.getTrader() == null)
		{
			this.container.player.closeScreen();
			return;
		}
		
		this.renderBackground(matrixStack);
		if(this.logWindow.visible)
		{
			this.logWindow.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonShowLog.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonClearLog.visible)
				this.buttonClearLog.render(matrixStack, mouseX, mouseY, partialTicks);
			IconAndButtonUtil.renderButtonTooltips(matrixStack, mouseX, mouseY, Lists.newArrayList(this.buttonShowLog, this.buttonClearLog));
			return;
		}
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
		
		IconAndButtonUtil.renderButtonTooltips(matrixStack, mouseX, mouseY, this.buttons);
		
		if(this.container.player.inventory.getItemStack().isEmpty())
		{
			int tradeCount = this.container.getTrader().getTradeCount();
			for(int i = 0; i < this.container.getTrader().getTradeCount(); i++)
			{
				boolean inverted = ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i);
				int result = ItemTradeButton.tryRenderTooltip(matrixStack, this, i, this.container.getTrader(), this.guiLeft + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.guiTop + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), inverted, mouseX, mouseY);
				if(result < 0 && this.container.hasPermission(Permissions.EDIT_TRADES)) //Result is negative if the mouse is over a slot, but the slot is empty.
					this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.item_edit"), mouseX, mouseY);
			}
		}
	}
	
	@Override
	public void tick()
	{
		if(this.container.getTrader() == null)
		{
			this.container.player.closeScreen();
			return;
		}
		if(!this.container.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.container.player.closeScreen();
			this.container.getTrader().sendOpenTraderMessage();
			return;
		}
		super.tick();
		
		this.buttonCollectMoney.visible = (!this.container.getTrader().getCoreSettings().isCreative() || this.container.getTrader().getStoredMoney().getRawValue() > 0) && this.container.hasPermission(Permissions.COLLECT_COINS) && !this.container.getTrader().getCoreSettings().hasBankAccount();
		this.buttonCollectMoney.active = this.container.getTrader().getStoredMoney().getRawValue() > 0;
		
		this.buttonOpenSettings.visible = this.container.hasPermission(Permissions.EDIT_SETTINGS);
		
		this.buttonStoreMoney.visible = this.container.HasCoinsToAdd() && this.container.hasPermission(Permissions.STORE_COINS);
		this.buttonClearLog.visible = this.container.getTrader().getLogger().logText.size() > 0 && this.container.hasPermission(Permissions.CLEAR_LOGS);
		
		this.buttonTradeRules.visible = this.container.hasPermission(Permissions.EDIT_TRADE_RULES);
		
	}
	
	//0 for left-click. 1 for right-click
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(!this.container.hasPermission(Permissions.EDIT_TRADES))
			return super.mouseClicked(mouseX, mouseY, button);
		ItemStack heldItem = this.container.player.inventory.getItemStack();
		int tradeCount = this.container.getTrader().getTradeCount();
		for(int i = 0; i < tradeCount; ++i)
		{
			ItemTradeData trade = this.container.getTrader().getTrade(i);
			if(ItemTradeButton.isMouseOverSlot(0, this.guiLeft + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.guiTop + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), (int)mouseX, (int)mouseY, ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i)))
			{
				ItemStack currentSellItem = trade.getSellItem();
				//LightmansCurrency.LogInfo("Clicked on sell item. Click Type: " + button + "; Sell Item: " + currentSellItem.getCount() + "x" + currentSellItem.getItem().getRegistryName() + "; Held Item: " + heldItem.getCount() + "x" + heldItem.getItem().getRegistryName());
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
					this.container.getTrader().sendSetTradeItemMessage(i, currentSellItem, 0);
					return true;
				}
				else
				{
					//If the held item is empty, right-click to increase by 1, left click to set to current held count
					if(button == 1)
					{
						if(InventoryUtil.ItemMatches(currentSellItem, heldItem))
						{
							if(currentSellItem.getCount() < currentSellItem.getMaxStackSize()) //Limit to the max stack size.
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
					this.container.getTrader().sendSetTradeItemMessage(i, currentSellItem, 0);
					return true;
				}
			}
			else if(this.container.getTrader().getTrade(i).isBarter() && ItemTradeButton.isMouseOverSlot(1, this.guiLeft + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.guiTop + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), (int)mouseX, (int)mouseY, ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i)))
			{
				ItemStack currentBarterItem = trade.getBarterItem();
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
					this.container.getTrader().sendSetTradeItemMessage(i, currentBarterItem, 1);
					return true;
				}
				else
				{
					//If the held item is not empty, right-click to increase by 1, left click to set to current held count
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
					this.container.getTrader().sendSetTradeItemMessage(i, currentBarterItem, 1);
					return true;
				}
			}
		}
		//LightmansCurrency.LogInfo("Did not click on any trade definition slots.");
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	private void PressTradesButton(Button button)
	{
		this.container.getTrader().sendOpenTraderMessage();
	}
	
	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(container.hasPermission(Permissions.COLLECT_COINS))
		{
			//CurrencyMod.LOGGER.info("Owner attempted to collect the stored money.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
		else
			Settings.PermissionWarning(this.container.player, "collect stored coins", Permissions.COLLECT_COINS);
	}
	
	private void PressStoreCoinsButton(Button button)
	{
		if(container.hasPermission(Permissions.STORE_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStoreCoins());
		}
		else
			Settings.PermissionWarning(this.container.player, "store coins", Permissions.STORE_COINS);
	}
	
	private void PressTradePriceButton(Button button)
	{
		if(!this.container.hasPermission(Permissions.EDIT_TRADES))
			return;
		
		int tradeIndex = 0;
		if(tradePriceButtons.contains(button))
			tradeIndex = tradePriceButtons.indexOf(button);
		
		this.minecraft.displayGuiScreen(new TradeItemPriceScreen(this.container::getTrader, tradeIndex, this.playerInventory.player));
		
	}
	
	private void PressLogButton(Button button)
	{
		this.logWindow.visible = !this.logWindow.visible;
	}
	
	private void PressClearLogButton(Button button)
	{
		this.container.getTrader().sendClearLogMessage();
	}
	
	private void PressTradeRulesButton(Button button)
	{
		Minecraft.getInstance().displayGuiScreen(new TradeRuleScreen(this.container.getTrader().getRuleScreenHandler()));
	}
	
	private void PressSettingsButton(Button button)
	{
		this.container.player.closeScreen();
		Minecraft.getInstance().displayGuiScreen(new TraderSettingsScreen(this.container::getTrader, (player) -> this.container.getTrader().sendOpenStorageMessage()));
	}
	
}
