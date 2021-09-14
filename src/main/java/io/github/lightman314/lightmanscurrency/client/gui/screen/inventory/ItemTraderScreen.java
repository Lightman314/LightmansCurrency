package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.*;
import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ItemTraderScreen extends AbstractContainerScreen<ItemTraderContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader.png");
	
	public static final int TRADEBUTTON_VERT_SPACER = ItemTraderUtil.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = ItemTraderUtil.TRADEBUTTON_VERTICALITY;
	public static final int TRADEBUTTON_HORIZ_SPACER = ItemTraderUtil.TRADEBUTTON_HORIZ_SPACER;
	public static final int TRADEBUTTON_HORIZONTAL = ItemTraderUtil.TRADEBUTTON_HORIZONTAL;
	
	Button buttonShowStorage;
	Button buttonCollectMoney;
	
	protected List<ItemTradeButton> tradeButtons = new ArrayList<>();
	
	public ItemTraderScreen(ItemTraderContainer container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = 133 + ItemTraderUtil.getTradeDisplayHeight(this.menu.getTradeCount());
		this.imageWidth = ItemTraderUtil.getWidth(this.menu.getTradeCount());
	}
	
	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		drawTraderBackground(matrix, this, this.menu, this.minecraft, this.imageWidth, this.imageHeight, this.menu.getTradeCount());
	}
	
	public static void drawTraderBackground(PoseStack matrix, Screen screen, AbstractContainerMenu container, Minecraft minecraft, int xSize, int ySize, int tradeCount)
	{
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		//RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		//minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (screen.width - xSize) / 2;
		int startY = (screen.height - ySize) / 2;
		
		int columnCount = ItemTraderUtil.getTradeDisplayColumnCount(tradeCount);
		int rowCount = ItemTraderUtil.getTradeDisplayRowCount(tradeCount);
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(tradeCount);
				
		//Top-left corner
		screen.blit(matrix, startX + ItemTraderUtil.getTradeDisplayOffset(tradeCount), startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, ItemTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY, 6 + ItemTradeButton.WIDTH, 0, TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		screen.blit(matrix, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(tradeCount) - 6, startY, 91, 0, 6, 17);
		
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
			screen.blit(matrix, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(tradeCount) - 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 91, 17, 6, TRADEBUTTON_VERTICALITY);
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
		screen.blit(matrix, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(tradeCount) - 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 91, 43, 6, 7);
		
		//Draw the bottom (player inventory/coin slots)
		screen.blit(matrix, startX + ItemTraderUtil.getInventoryDisplayOffset(tradeCount), startY + ItemTraderUtil.getTradeDisplayHeight(tradeCount), 0, 50, 176, 133);
		
		CoinSlot.drawEmptyCoinSlots(screen, container, matrix, startX, startY);
	}
	
	@Override
	protected void renderLabels(PoseStack matrix, int mouseX, int mouseY)
	{
		
		drawTraderForeground(matrix, this.font, this.menu.getTradeCount(), this.imageHeight,
				this.menu.blockEntity.getTitle(),
				this.playerInventoryTitle,
				new TranslatableComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.menu.GetCoinValue())));
		
	}
	
	public static void drawTraderForeground(PoseStack matrix, Font font, int tradeCount, int ySize, Component title, Component inventoryTitle, Component creditText)
	{
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(tradeCount);
		int inventoryOffset = ItemTraderUtil.getInventoryDisplayOffset(tradeCount);
		
		font.draw(matrix, title.getString(), tradeOffset + 8.0f, 6.0f, 0x404040);
		
		font.draw(matrix, inventoryTitle.getString(), inventoryOffset + 8.0f, (ySize - 94), 0x404040);
		
		font.draw(matrix, creditText.getString(), inventoryOffset + 80f, ySize - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		if(this.menu.isOwner())
		{
			
			int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.menu.getTradeCount());
			
			this.buttonShowStorage = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos, this::PressStorageButton, GUI_TEXTURE, 176, 0));
			
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
			this.tradeButtons.add(this.addRenderableWidget(new ItemTradeButton(this.leftPos + ItemTraderUtil.getButtonPosX(tradeCount, i), this.topPos + ItemTraderUtil.getButtonPosY(tradeCount, i), this::PressTradeButton, menu.blockEntity.getTrade(i), this.font, () -> this.menu.blockEntity, this.menu)));
		}
	}
	
	@Override
	public void containerTick()
	{
		
		this.menu.tick();
		
		for(int i = 0; i < tradeButtons.size(); i++)
		{
			tradeButtons.get(i).UpdateTrade(menu.blockEntity.getTrade(i));
		}
		
		if(this.buttonCollectMoney != null)
		{
			this.buttonCollectMoney.active = this.menu.blockEntity.getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.menu.blockEntity.isCreative();
		}
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonShowStorage != null && this.buttonShowStorage.isMouseOver(mouseX,mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.openstorage"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.blockEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		
	}
	
	private void PressStorageButton(Button button)
	{
		//Open the container screen
		if(menu.isOwner())
		{
			//CurrencyMod.LOGGER.info("Owner attempted to open the Trader's Storage.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.menu.blockEntity.getBlockPos()));
		}
		else
			LightmansCurrency.LogWarning("Non-owner attempted to open the Trader's Storage.");
	}
	
	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(menu.isOwner())
		{
			//LightmansCurrency.LOGGER.info("Owner attempted to collect the stored money.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
		else
			LightmansCurrency.LogWarning("Non-owner attempted to collect the stored money.");
	}
	
	private void PressTradeButton(Button button)
	{
		
		int tradeIndex = 0;
		if(tradeButtons.contains(button))
			tradeIndex = tradeButtons.indexOf(button);
		
		LightmansCurrency.LogWarning("Trade Button clicked for index " + tradeIndex + ".");
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
		
	}
	
}
