package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeItemPriceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderNameScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageSetTradeItem;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearLogger;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveAlly;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenTrades;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageToggleCreative;
import io.github.lightman314.lightmanscurrency.tileentity.IPermissions;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ItemTraderStorageScreen extends AbstractContainerScreen<ItemTraderStorageContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/traderstorage.png");
	public static final ResourceLocation ALLY_GUI_TEXTURE = TradeRuleScreen.GUI_TEXTURE;
	
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
	
	boolean allyScreenOpen = false;
	Button buttonAllies;
	Button buttonAddAlly;
	Button buttonRemoveAlly;
	EditBox allyTextInput;
	
	List<Button> tradePriceButtons = new ArrayList<>();
	
	public ItemTraderStorageScreen(ItemTraderStorageContainer container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		int tradeCount = this.menu.tileEntity.getTradeCount();
		this.imageHeight = 18 * ItemTraderStorageUtil.getRowCount(tradeCount) + 125;
		this.imageWidth = ItemTraderStorageUtil.getWidth(tradeCount);
		
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		drawTraderStorageBackground(poseStack, this, this.font, this.menu, this.menu.tileEntity, this.minecraft, this.imageWidth, this.imageHeight);
		
	}
	
	public static void drawTraderStorageBackground(PoseStack poseStack, Screen screen, Font font, AbstractContainerMenu container, IItemTrader trader, Minecraft minecraft, int imageWidth, int imageHeight)
	{
		
		List<ItemTradeData> trades = trader.getAllTrades();
		int tradeCount = trades.size();
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int startX = (screen.width - imageWidth) / 2;
		int startY = (screen.height - imageHeight) / 2;
		
		int rowCount = ItemTraderStorageUtil.getRowCount(tradeCount);
		int columnCount = ItemTraderStorageUtil.getColumnCount(tradeCount);
		
		//Top-left corner
		screen.blit(poseStack, startX + SCREEN_EXTENSION, startY, 0, 0, 7, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Draw the top
			screen.blit(poseStack, startX + SCREEN_EXTENSION + 7 + (x * 162), startY, 7, 0, 162, 17);
		}
		//Top-right corner
		screen.blit(poseStack, startX + imageWidth - SCREEN_EXTENSION - 7, startY, 169, 0, 7, 17);
		
		//Draw the storage rows
		int index = 0;
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			screen.blit(poseStack, startX + SCREEN_EXTENSION, startY + 17 + 18 * y, 0, 17, 7, 18);
			if(y < rowCount - 1 || tradeCount % ItemTraderStorageUtil.getColumnCount(tradeCount) == 0 || y == 0)
			{
				for(int x = 0; x < columnCount; x++)
				{
					if(index < tradeCount)
						screen.blit(poseStack, startX + SCREEN_EXTENSION + 7 + (x * 162), startY + 17 + 18 * y, 7, 17, 162, 18);
					else
						screen.blit(poseStack, startX + SCREEN_EXTENSION + 7 + (x * 162), startY + 17 + 18 * y, 0, 143, 162, 18);
					index++;
				}
			}
			else //Trade Count is NOT even, and is on the last row, AND is not the first row
			{
				for(int x = 0; x < columnCount; x++)
				{
					//Draw a blank background
					screen.blit(poseStack, startX + SCREEN_EXTENSION + 7 + (x * 162), startY + 17 + 18 * y, 0, 143, 162, 18);
				}
				screen.blit(poseStack, startX + SCREEN_EXTENSION + 7 + ItemTraderStorageUtil.getStorageSlotOffset(tradeCount, y), startY + 17 + 18 * y, 7, 17, 162, 18);
			}
			//Right edge
			screen.blit(poseStack, startX + imageWidth - SCREEN_EXTENSION - 7, startY + 17 + 18 * y, 169, 17, 7, 18);
		}
		
		//Bottom-left corner
		screen.blit(poseStack, startX + SCREEN_EXTENSION, startY + 17 + 18 * rowCount, 0, 35, 7, 8);
		for(int x = 0; x < columnCount; x++)
		{
			//Draw the bottom
			screen.blit(poseStack, startX + SCREEN_EXTENSION + 7 + (x * 162), startY + 17 + 18 * rowCount, 7, 35, 162, 8);
		}
		//Bottom-right corner
		screen.blit(poseStack, startX + imageWidth - SCREEN_EXTENSION - 7, startY + 17 + 18 * rowCount, 169, 35, 7, 8);
		
		//Draw the bottom
		screen.blit(poseStack, startX + SCREEN_EXTENSION + ItemTraderStorageUtil.getInventoryOffset(tradeCount), startY + 25 + rowCount * 18, 0, 43, 176 + 32, 100);
		
		//Render the fake trade buttons
		for(int i = 0; i < tradeCount; i++)
		{
			boolean inverted = ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i);
			ItemTradeButton.renderItemTradeButton(poseStack, screen, font, startX + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), startY + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), i, trader, null, false, true, inverted);
		}
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		
		drawTraderStorageForeground(poseStack, this.font, this.menu.tileEntity.getTradeCount(), this.imageHeight, this.menu.tileEntity.getName(), this.playerInventoryTitle);
		
	}
	
	//Trade count is now only required to get the proper positioning for the inventory label
	public static void drawTraderStorageForeground(PoseStack poseStack, Font font, int tradeCount, int ySize, Component title, Component inventoryTitle)
	{
		
		font.draw(poseStack, title, 8.0f + SCREEN_EXTENSION, 6.0f, 0x404040);

		font.draw(poseStack, inventoryTitle, ItemTraderStorageUtil.getInventoryOffset(tradeCount) + 8.0f + SCREEN_EXTENSION, (ySize - 94), 0x404040);
		
	}
	
	public static void drawAllyScreen(PoseStack poseStack, Screen screen, Font font, IPermissions allySource, Minecraft minecraft, int xSize, int ySize)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, ALLY_GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int startX = ((screen.width - xSize) / 2) + (xSize/2) - 176/2;
		int startY = (screen.height - ySize) / 2;
		
		screen.blit(poseStack, startX, startY, 0, 0, 176, 176);
		
		screen.blit(poseStack, startX, startY + 55, 0, 176, 176, 80);
		screen.blit(poseStack, startX, startY + 135, 0, 176, 176, 34);
		
		int x = 0;
		int y = 0;
		for(int i = 0; i < allySource.getAllies().size() && x < 2; i++)
		{
			
			font.draw(poseStack, allySource.getAllies().get(i), startX + 10 + 78 * x, startY + 57 + 10 * y, 0xFFFFFF);
			y++;
			if(y > 10)
			{
				y = 0;
				x++;
			}
		}
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		this.buttonShowTrades = this.addRenderableWidget(new IconButton(this.leftPos + SCREEN_EXTENSION, this.topPos - 20, this::PressTradesButton, GUI_TEXTURE, 176, 0));
		
		this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos + SCREEN_EXTENSION + 20, this.topPos - 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = !this.menu.tileEntity.isCreative() && this.menu.isOwner();
		
		int tradeCount = this.menu.tileEntity.getTradeCount();
		this.buttonStoreMoney = this.addRenderableWidget(new IconButton(this.leftPos + SCREEN_EXTENSION + ItemTraderStorageUtil.getInventoryOffset(tradeCount) + 176 + 32, this.topPos + 25 + ItemTraderStorageUtil.getRowCount(tradeCount) * 18, this::PressStoreCoinsButton, GUI_TEXTURE, 176, 16));
		this.buttonStoreMoney.visible = false;
		
		this.buttonChangeName = this.addRenderableWidget(new Button(this.leftPos + SCREEN_EXTENSION + 40, this.topPos - 20, 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.changename"), this::PressTraderNameButton));
		this.buttonChangeName.visible = this.menu.isOwner();
		this.buttonShowLog = this.addRenderableWidget(new Button(this.leftPos + SCREEN_EXTENSION + 60, this.topPos - 20, 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.showlog"), this::PressLogButton));
		this.buttonClearLog = this.addRenderableWidget(new Button(this.leftPos + SCREEN_EXTENSION + 80, this.topPos - 20, 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.clearlog"), this::PressClearLogButton));
		
		this.buttonToggleCreative = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth - SCREEN_EXTENSION - 40, this.topPos - 20, this::PressCreativeButton, GUI_TEXTURE, 176 + 32, 0));
		this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.menu.player);
		this.buttonAddTrade = this.addRenderableWidget(new PlainButton(this.leftPos + this.imageWidth - SCREEN_EXTENSION - 50, this.topPos - 20, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 0));
		this.buttonAddTrade.visible = this.menu.tileEntity.isCreative() && TradingOffice.isAdminPlayer(this.menu.player);;
		this.buttonAddTrade.active = this.menu.tileEntity.getTradeCount() < ItemTraderTileEntity.TRADELIMIT;
		this.buttonRemoveTrade = this.addRenderableWidget(new PlainButton(this.leftPos + this.imageWidth - SCREEN_EXTENSION - 50, this.topPos - 10, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 20));
		this.buttonRemoveTrade.visible = this.menu.tileEntity.isCreative() && TradingOffice.isAdminPlayer(this.menu.player);
		this.buttonRemoveTrade.active = this.menu.tileEntity.getTradeCount() > 1;
			
		if(this.menu.isOwner())
		{
			
			this.buttonAllies = this.addRenderableWidget(new IconButton(this.leftPos + SCREEN_EXTENSION + 100, this.topPos - 20, this::PressAllyButton, GUI_TEXTURE, 176 + 32, 16));
			
			this.allyTextInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + this.imageWidth/2 - 176/2 + 10, this.topPos + 9, 176 - 20, 20, new TextComponent("")));
			this.allyTextInput.setMaxLength(32);
			this.allyTextInput.visible = false;
			
			this.buttonAddAlly = this.addRenderableWidget(new Button(this.leftPos + this.imageWidth/2 - 176/2 + 10, this.topPos + 30, 78, 20, new TranslatableComponent("gui.button.lightmanscurrency.allies.add"), this::PressAddAllyButton));
			this.buttonAddAlly.visible = false;
			this.buttonRemoveAlly = this.addRenderableWidget(new Button(this.leftPos + this.imageWidth/2 - 176/2 + 88, this.topPos + 30, 78, 20, new TranslatableComponent("gui.button.lightmanscurrency.allies.remove"), this::PressRemoveAllyButton));
			this.buttonRemoveAlly.visible = false;
			
		}
		
		this.buttonTradeRules = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth - SCREEN_EXTENSION - 20, this.topPos - 20, this::PressTradeRulesButton, GUI_TEXTURE, 176 + 16, 16));
		
		this.logWindow = this.addWidget(new TextLogWindow(this.leftPos + (this.imageWidth / 2) - (TextLogWindow.WIDTH / 2), this.topPos, () -> this.menu.tileEntity.getLogger(), this.font));
		this.logWindow.visible = false;
		
		for(int i = 0; i < tradeCount; i++)
		{
			tradePriceButtons.add(this.addRenderableWidget(new Button(this.leftPos + ItemTraderStorageUtil.getTradePriceButtonPosX(tradeCount, i), this.topPos + ItemTraderStorageUtil.getTradePriceButtonPosY(tradeCount, i), 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.dollarsign"), this::PressTradePriceButton)));
		}
		
		tick();
		
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
		else if(this.allyScreenOpen)
		{
			
			drawAllyScreen(matrixStack, this, this.font, this.menu.tileEntity, minecraft, this.imageWidth, this.imageHeight);
			
			this.allyTextInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonAddAlly.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonRemoveAlly.render(matrixStack, mouseX, mouseY, partialTicks);
			
			if(this.buttonAllies != null && this.buttonAllies.isMouseOver(mouseX, mouseY))
			{
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.allies"), mouseX, mouseY);
			}
			this.buttonAllies.render(matrixStack, mouseX, mouseY, partialTicks);
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
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonStoreMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.storecoins"), mouseX, mouseY);
		}
		else if(this.buttonToggleCreative.visible && this.buttonToggleCreative.isMouseOver(mouseX, mouseY))
		{
			if(this.menu.tileEntity.isCreative())
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
		else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
		}
		else if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		}
		else if(this.buttonAllies != null && this.buttonAllies.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.allies"), mouseX, mouseY);
		}
		else if(this.menu.getCarried().isEmpty())
		{
			int tradeCount = this.menu.tileEntity.getTradeCount();
			for(int i = 0; i < this.menu.tileEntity.getTradeCount(); i++)
			{
				boolean inverted = ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i);
				int result = ItemTradeButton.tryRenderTooltip(matrixStack, this, i, this.menu.tileEntity, this.leftPos + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.topPos + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), inverted, mouseX, mouseY, null);
				if(result < 0) //Result is negative if the mouse is over a slot, but the slot is empty.
					this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.item_edit"), mouseX, mouseY);
			}
		}
	}
	
	@Override
	public void containerTick()
	{
		if(!this.menu.hasPermissions())
		{
			this.menu.player.closeContainer();
			return;
		}
		super.tick();
		
		this.menu.tick();
		
		this.buttonCollectMoney.visible = (!this.menu.tileEntity.isCreative() || this.menu.tileEntity.getStoredMoney().getRawValue() > 0) && this.menu.isOwner();
		this.buttonCollectMoney.active = this.menu.tileEntity.getStoredMoney().getRawValue() > 0;
		
		this.buttonStoreMoney.visible = this.menu.HasCoinsToAdd();
		this.buttonClearLog.visible = this.menu.tileEntity.getLogger().logText.size() > 0 && this.menu.isOwner();
		
		if(this.menu.isOwner())
		{
			this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.menu.player);
			if(this.buttonToggleCreative.visible)
			{
				if(this.menu.tileEntity.isCreative())
				{
					this.buttonToggleCreative.setResource(GUI_TEXTURE, 176 + 32, 0);
					this.buttonAddTrade.visible = true;
					this.buttonAddTrade.active = this.menu.tileEntity.getTradeCount() < ItemTraderTileEntity.TRADELIMIT;
					this.buttonRemoveTrade.visible = true;
					this.buttonRemoveTrade.active = this.menu.tileEntity.getTradeCount() > 1;
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
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if(this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.allyScreenOpen)
			return false;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	//0 for left-click. 1 for right-click
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		ItemStack heldItem = this.menu.getCarried();
		int tradeCount = this.menu.tileEntity.getTradeCount();
		for(int i = 0; i < tradeCount; ++i)
		{
			ItemTradeData trade = this.menu.tileEntity.getTrade(i);
			if(ItemTradeButton.isMouseOverSlot(0, this.leftPos + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.topPos + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), (int)mouseX, (int)mouseY, ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i)))
			{
				ItemStack currentSellItem = trade.getSellItem();
				//LightmansCurrency.LogInfo("Clicked on sell item. Click Type: " + button + "; Sell Item: " + currentSellItem.getCount() + "x" + currentSellItem.getItem().getRegistryName() + "; Held Item: " + heldItem.getCount() + "x" + heldItem.getItem().getRegistryName());
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
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem(this.menu.tileEntity.getBlockPos(), i, currentSellItem, 0));
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
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem(this.menu.tileEntity.getBlockPos(), i, currentSellItem, 0));
					return true;
				}
			}
			else if(this.menu.tileEntity.getTrade(i).isBarter() && ItemTradeButton.isMouseOverSlot(1, this.leftPos + ItemTraderStorageUtil.getFakeTradeButtonPosX(tradeCount, i), this.topPos + ItemTraderStorageUtil.getFakeTradeButtonPosY(tradeCount, i), (int)mouseX, (int)mouseY, ItemTraderStorageUtil.isFakeTradeButtonInverted(tradeCount, i)))
			{
				ItemStack currentBarterItem = trade.getBarterItem();
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
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem(this.menu.tileEntity.getBlockPos(), i, currentBarterItem, 1));
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
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem(this.menu.tileEntity.getBlockPos(), i, currentBarterItem, 1));
					return true;
				}
			}
		}
		//LightmansCurrency.LogInfo("Did not click on any trade definition slots.");
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	private void PressTradesButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.menu.tileEntity.getBlockPos()));
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
		
		this.minecraft.setScreen(new TradeItemPriceScreen(this.menu.tileEntity, tradeIndex, this.menu.player));
		
	}
	
	private void PressTraderNameButton(Button button)
	{
		this.minecraft.setScreen(new TraderNameScreen(this.menu.tileEntity, this.menu.player));
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
	
	private void PressLogButton(Button button)
	{
		this.logWindow.visible = !this.logWindow.visible;
	}
	
	private void PressClearLogButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearLogger(this.menu.tileEntity.getBlockPos()));
	}
	
	private void PressTradeRulesButton(Button button)
	{
		Minecraft.getInstance().setScreen(new TradeRuleScreen(this.menu.tileEntity.GetRuleScreenBackHandler()));
	}
	
	private void PressAllyButton(Button button)
	{
		this.allyScreenOpen = !this.allyScreenOpen;
	}
	
	private void PressAddAllyButton(Button button)
	{
		String newAlly = this.allyTextInput.getValue();
		this.allyTextInput.setValue("");
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveAlly(this.menu.tileEntity.getBlockPos(), true, newAlly));
	}
	
	private void PressRemoveAllyButton(Button button)
	{
		String newAlly = this.allyTextInput.getValue();
		this.allyTextInput.setValue("");
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveAlly(this.menu.tileEntity.getBlockPos(), false, newAlly));
	}
	
}
