package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemTraderScreen extends ContainerScreen<ItemTraderContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader.png");
	
	public static final int TRADEBUTTON_VERT_SPACER = ItemTraderUtil.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = ItemTraderUtil.TRADEBUTTON_VERTICALITY;
	public static final int TRADEBUTTON_HORIZ_SPACER = ItemTraderUtil.TRADEBUTTON_HORIZ_SPACER;
	public static final int TRADEBUTTON_HORIZONTAL = ItemTraderUtil.TRADEBUTTON_HORIZONTAL;
	
	Button buttonShowStorage;
	Button buttonCollectMoney;
	
	protected List<ItemTradeButton> tradeButtons = new ArrayList<>();
	
	public ItemTraderScreen(ItemTraderContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.ySize = 133 + ItemTraderUtil.getTradeDisplayHeight(this.container.tileEntity);
		this.xSize = ItemTraderUtil.getWidth(this.container.tileEntity);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		drawTraderBackground(matrix, this, this.container, this.minecraft, this.xSize, this.ySize, this.container.tileEntity);
		
	}
	
	@SuppressWarnings("deprecation")
	public static void drawTraderBackground(MatrixStack matrix, Screen screen, Container container, Minecraft minecraft, int xSize, int ySize, IItemTrader trader)
	{
		//int tradeCount = trader.getTradeCount();
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (screen.width - xSize) / 2;
		int startY = (screen.height - ySize) / 2;
		
		int columnCount = ItemTraderUtil.getTradeDisplayColumnCount(trader);
		int rowCount = ItemTraderUtil.getTradeDisplayRowCount(trader);
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(trader);
				
		//Top-left corner
		screen.blit(matrix, startX + ItemTraderUtil.getTradeDisplayOffset(trader), startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, ItemTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY, 6 + ItemTradeButton.WIDTH, 0, TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		screen.blit(matrix, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(trader) - 6, startY, 91, 0, 6, 17);
		
		//Draw the bg & spacer of each button
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			screen.blit(matrix, startX + tradeOffset, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 0, 17, 6, TRADEBUTTON_VERTICALITY);
			for(int x = 0; x < columnCount; x++)
			{
				//Button BG
				screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6, 17, ItemTradeButton.WIDTH, TRADEBUTTON_VERTICALITY);
				//Right spacer for the trade button
				if(x < columnCount - 1)
					screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6 + ItemTradeButton.WIDTH, 17, TRADEBUTTON_HORIZ_SPACER, TRADEBUTTON_VERTICALITY);
			}
			//Right edge
			screen.blit(matrix, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 91, 17, 6, TRADEBUTTON_VERTICALITY);
		}
		
		//Bottom-left corner
		screen.blit(matrix, startX + tradeOffset, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 0, 43, 6, 7);
		for(int x = 0; x < columnCount; x++)
		{
			//Bottom of each button
			screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 43, ItemTradeButton.WIDTH, 7);
			//Bottom spacer of each button
			if(x < columnCount - 1)
				screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 43, TRADEBUTTON_HORIZ_SPACER, 7);
		}
		//Bottom-right corner
		screen.blit(matrix, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 91, 43, 6, 7);
		
		//Draw the bottom (player inventory/coin slots)
		screen.blit(matrix, startX + ItemTraderUtil.getInventoryDisplayOffset(trader), startY + ItemTraderUtil.getTradeDisplayHeight(trader), 0, 50, 176, 133);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		drawTraderForeground(matrix, this.font, this.container.tileEntity, this.ySize,
				this.container.tileEntity.getTitle(),
				this.playerInventory.getDisplayName(),
				new TranslationTextComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.container.GetCoinValue())));
		
	}
	
	public static void drawTraderForeground(MatrixStack matrix, FontRenderer font, IItemTrader trader, int ySize, ITextComponent title, ITextComponent inventoryTitle, ITextComponent creditText)
	{
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(trader);
		int inventoryOffset = ItemTraderUtil.getInventoryDisplayOffset(trader);
		
		font.drawString(matrix, title.getString(), tradeOffset + 8.0f, 6.0f, 0x404040);
		
		font.drawString(matrix, inventoryTitle.getString(), inventoryOffset + 8.0f, (ySize - 94), 0x404040);
		
		font.drawString(matrix, creditText.getString(), inventoryOffset + 80f, ySize - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.container.tileEntity);
		
		this.buttonShowStorage = this.addButton(new IconButton(this.guiLeft - 20 + tradeOffset, this.guiTop, this::PressStorageButton, this.font, IconData.of(Items.CHEST)));
		this.buttonShowStorage.visible = this.container.hasPermission(Permissions.OPEN_STORAGE);
		
		this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft - 20 + tradeOffset, this.guiTop + 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 0)));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.container.hasPermission(Permissions.COLLECT_COINS) && !this.container.tileEntity.getCoreSettings().hasBankAccount();
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.container.tileEntity.getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addButton(new ItemTradeButton(this.guiLeft + ItemTraderUtil.getButtonPosX(this.container.tileEntity, i), this.guiTop + ItemTraderUtil.getButtonPosY(this.container.tileEntity, i), this::PressTradeButton, i, this, this.font, () -> this.container.tileEntity, this.container)));
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		this.container.tick();
		
		this.buttonShowStorage.visible = this.container.hasPermission(Permissions.OPEN_STORAGE);
		if(this.container.hasPermission(Permissions.COLLECT_COINS))
		{
			this.buttonCollectMoney.visible = !this.container.tileEntity.getCoreSettings().hasBankAccount();
			this.buttonCollectMoney.active = this.container.tileEntity.getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.container.tileEntity.getCoreSettings().isCreative();
		}
		else
			this.buttonCollectMoney.visible = false;
		
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonShowStorage != null && this.buttonShowStorage.isMouseOver(mouseX,mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.openstorage"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.collectcoins", this.container.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(matrixStack, this, this.container.tileEntity, false, mouseX, mouseY, this.container);
		}
	}
	
	private void PressStorageButton(Button button)
	{
		//Open the container screen
		if(this.container.hasPermission(Permissions.OPEN_STORAGE))
		{
			//CurrencyMod.LOGGER.info("Owner attempted to open the Trader's Storage.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.container.tileEntity.getPos()));
		}
	}
	
	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(this.container.hasPermission(Permissions.COLLECT_COINS))
		{
			//LightmansCurrency.LOGGER.info("Owner attempted to collect the stored money.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
	}
	
	private void PressTradeButton(Button button)
	{
		
		int tradeIndex = 0;
		if(tradeButtons.contains(button))
			tradeIndex = tradeButtons.indexOf(button);
		
		//LightmansCurrency.LogWarning("Trade Button clicked for index " + tradeIndex + ".");
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
		
	}
	
}
