package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.*;
import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainer;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
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
		this.imageHeight = 133 + ItemTraderUtil.getTradeDisplayHeight(this.menu.tileEntity);
		this.imageWidth = ItemTraderUtil.getWidth(this.menu.tileEntity);
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		drawTraderBackground(poseStack, this, this.menu, this.minecraft, this.imageWidth, this.imageHeight, this.menu.tileEntity);
		
	}
	
	public static void drawTraderBackground(PoseStack poseStack, Screen screen, AbstractContainerMenu container, Minecraft minecraft, int imageWidth, int imageHeight, IItemTrader trader)
	{
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int startX = (screen.width - imageWidth) / 2;
		int startY = (screen.height - imageHeight) / 2;
		
		int columnCount = ItemTraderUtil.getTradeDisplayColumnCount(trader);
		int rowCount = ItemTraderUtil.getTradeDisplayRowCount(trader);
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(trader);
				
		//Top-left corner
		screen.blit(poseStack, startX + ItemTraderUtil.getTradeDisplayOffset(trader), startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, ItemTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY, 6 + ItemTradeButton.WIDTH, 0, TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		screen.blit(poseStack, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(trader) - 6, startY, 91, 0, 6, 17);
		
		//Draw the bg & spacer of each button
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			screen.blit(poseStack, startX + tradeOffset, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 0, 17, 6, TRADEBUTTON_VERTICALITY);
			for(int x = 0; x < columnCount; x++)
			{
				//Button BG
				screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6, 17, ItemTradeButton.WIDTH, TRADEBUTTON_VERTICALITY);
				//Right spacer for the trade button
				if(x < columnCount - 1)
					screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6 + ItemTradeButton.WIDTH, 17, TRADEBUTTON_HORIZ_SPACER, TRADEBUTTON_VERTICALITY);
			}
			//Right edge
			screen.blit(poseStack, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 91, 17, 6, TRADEBUTTON_VERTICALITY);
		}
		
		//Bottom-left corner
		screen.blit(poseStack, startX + tradeOffset, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 0, 43, 6, 7);
		for(int x = 0; x < columnCount; x++)
		{
			//Bottom of each button
			screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 43, ItemTradeButton.WIDTH, 7);
			//Bottom spacer of each button
			if(x < columnCount - 1)
				screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 43, TRADEBUTTON_HORIZ_SPACER, 7);
		}
		//Bottom-right corner
		screen.blit(poseStack, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 91, 43, 6, 7);
		
		//Draw the bottom (player inventory/coin slots)
		screen.blit(poseStack, startX + ItemTraderUtil.getInventoryDisplayOffset(trader), startY + ItemTraderUtil.getTradeDisplayHeight(trader), 0, 50, 176, 133);
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		
		drawTraderForeground(poseStack, this.font, this.menu.tileEntity, this.imageHeight,
				this.menu.tileEntity.getTitle(),
				this.playerInventoryTitle,
				new TranslatableComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.menu.GetCoinValue())));
		
	}
	
	public static void drawTraderForeground(PoseStack poseStack, Font font, IItemTrader trader, int ySize, Component title, Component inventoryTitle, Component creditText)
	{
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(trader);
		int inventoryOffset = ItemTraderUtil.getInventoryDisplayOffset(trader);
		
		font.draw(poseStack, title, tradeOffset + 8.0f, 6.0f, 0x404040);
		
		font.draw(poseStack, inventoryTitle, inventoryOffset + 8.0f, (ySize - 94), 0x404040);
		
		font.draw(poseStack, creditText, inventoryOffset + 80f, ySize - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		if(this.menu.hasPermissions())
		{
			
			int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.menu.tileEntity);
			
			this.buttonShowStorage = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos, this::PressStorageButton, GUI_TEXTURE, 176, 0));
			
			if(this.menu.isOwner())
			{
				this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos + 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
				this.buttonCollectMoney.active = false;
			}
			
		}
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.menu.tileEntity.getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addRenderableWidget(new ItemTradeButton(this.leftPos + ItemTraderUtil.getButtonPosX(this.menu.tileEntity, i), this.topPos + ItemTraderUtil.getButtonPosY(this.menu.tileEntity, i), this::PressTradeButton, i, this, this.font, () -> this.menu.tileEntity, this.menu)));
		}
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
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(matrixStack, this, this.menu.tileEntity, false, mouseX, mouseY, this.menu);
		}
	}
	
	private void PressStorageButton(Button button)
	{
		//Open the container screen
		if(menu.hasPermissions())
		{
			//CurrencyMod.LOGGER.info("Owner attempted to open the Trader's Storage.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.menu.tileEntity.getBlockPos()));
		}
		else
			LightmansCurrency.LogWarning("Player without permissions attempted to open the Trader's Storage.");
	}
	
	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(menu.isOwner())
		{
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
		
		//LightmansCurrency.LogWarning("Trade Button clicked for index " + tradeIndex + ".");
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
		
	}
	
}
