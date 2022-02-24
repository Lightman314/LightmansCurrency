package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.*;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRNextTrader;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRSkipTo;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.menus.ItemTraderMenu;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@IPNIgnore
public class ItemTraderScreen extends AbstractContainerScreen<ItemTraderMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader.png");
	
	public static final int TRADEBUTTON_VERT_SPACER = ItemTraderUtil.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = ItemTraderUtil.TRADEBUTTON_VERTICALITY;
	public static final int TRADEBUTTON_HORIZ_SPACER = ItemTraderUtil.TRADEBUTTON_HORIZ_SPACER;
	public static final int TRADEBUTTON_HORIZONTAL = ItemTraderUtil.TRADEBUTTON_HORIZONTAL;
	
	Button buttonShowStorage;
	Button buttonCollectMoney;
	
	Button buttonBack;
	
	Button buttonLeft;
	Button buttonRight;
	
	EditBox pageInput;
	Button buttonSkipToPage;
	
	protected List<ItemTradeButton> tradeButtons = new ArrayList<>();
	
	public ItemTraderScreen(ItemTraderMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = 133 + ItemTraderUtil.getTradeDisplayHeight(this.menu.getTrader());
		this.imageWidth = ItemTraderUtil.getWidth(this.menu.getTrader());
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		if(this.menu.getTrader() == null)
			return;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int startX = (this.width - imageWidth) / 2;
		int startY = (this.height - imageHeight) / 2;
		
		int columnCount = ItemTraderUtil.getTradeDisplayColumnCount(this.menu.getTrader());
		int rowCount = ItemTraderUtil.getTradeDisplayRowCount(this.menu.getTrader());
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.menu.getTrader());
				
		//Top-left corner
		this.blit(poseStack, startX + ItemTraderUtil.getTradeDisplayOffset(this.menu.getTrader()), startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, ItemTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY, 6 + ItemTradeButton.WIDTH, 0, TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		this.blit(poseStack, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(this.menu.getTrader()) - 6, startY, 91, 0, 6, 17);
		
		//Draw the bg & spacer of each button
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			this.blit(poseStack, startX + tradeOffset, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 0, 17, 6, TRADEBUTTON_VERTICALITY);
			for(int x = 0; x < columnCount; x++)
			{
				//Button BG
				this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6, 17, ItemTradeButton.WIDTH, TRADEBUTTON_VERTICALITY);
				//Right spacer for the trade button
				if(x < columnCount - 1)
					this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6 + ItemTradeButton.WIDTH, 17, TRADEBUTTON_HORIZ_SPACER, TRADEBUTTON_VERTICALITY);
			}
			//Right edge
			this.blit(poseStack, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(this.menu.getTrader()) - 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 91, 17, 6, TRADEBUTTON_VERTICALITY);
		}
		
		//Bottom-left corner
		this.blit(poseStack, startX + tradeOffset, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 0, 43, 6, 7);
		for(int x = 0; x < columnCount; x++)
		{
			//Bottom of each button
			this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 43, ItemTradeButton.WIDTH, 7);
			//Bottom spacer of each button
			if(x < columnCount - 1)
				this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 43, TRADEBUTTON_HORIZ_SPACER, 7);
		}
		//Bottom-right corner
		this.blit(poseStack, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(this.menu.getTrader()) - 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 91, 43, 6, 7);
		
		//Draw the bottom (player inventory/coin slots)
		this.blit(poseStack, startX + ItemTraderUtil.getInventoryDisplayOffset(this.menu.getTrader()), startY + ItemTraderUtil.getTradeDisplayHeight(this.menu.getTrader()), 0, 50, 176, 133);
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		
		if(this.menu.getTrader() == null)
			return;
		
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.menu.getTrader());
		int inventoryOffset = ItemTraderUtil.getInventoryDisplayOffset(this.menu.getTrader());
		
		font.draw(poseStack, this.menu.getTrader().getTitle(), tradeOffset + 8.0f, 6.0f, 0x404040);
		
		font.draw(poseStack, this.playerInventoryTitle, inventoryOffset + 8.0f, (this.imageHeight - 94), 0x404040);
		
		font.draw(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.menu.GetCoinValue())), inventoryOffset + 80f, this.imageHeight - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.menu.getTrader());
		int inventoryOffset = ItemTraderUtil.getInventoryDisplayOffset(this.menu.getTrader());
		
		this.buttonShowStorage = this.addRenderableWidget(IconAndButtonUtil.storageButton(this.leftPos - 20 + tradeOffset, this.topPos, this::PressStorageButton));
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE) && !this.menu.isCashRegister();
		
		this.buttonCollectMoney = this.addRenderableWidget(IconAndButtonUtil.collectCoinButton(this.leftPos - 20 + tradeOffset, this.topPos + 20, this::PressCollectionButton, this.menu::getTrader));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getTrader().getCoreSettings().hasBankAccount();
		
		if(this.menu.isUniversal())
			this.buttonBack = this.addRenderableWidget(IconAndButtonUtil.backToTerminalButton(this.leftPos - 20 + inventoryOffset, this.topPos + this.imageHeight - 20, this::PressBackButton));
		
		if(this.menu.isCashRegister() && this.menu.getTotalCRSize() > 1)
		{
			this.buttonLeft = this.addRenderableWidget(IconAndButtonUtil.leftButton(this.leftPos + tradeOffset - 20,  this.topPos, this::PressArrowButton));
			this.buttonRight = this.addRenderableWidget(IconAndButtonUtil.rightButton(this.leftPos + this.imageWidth - tradeOffset, this.topPos, this::PressArrowButton));
			
			this.pageInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 50, this.topPos - 19, this.imageWidth - 120, 18, new TextComponent("")));
			this.pageInput.setMaxLength(9);
			this.pageInput.setValue(String.valueOf(this.menu.getThisCRIndex() + 1));
			
			this.buttonSkipToPage = this.addRenderableWidget(IconAndButtonUtil.rightButton(this.leftPos + this.imageWidth - 68,  this.topPos - 20, this::PressPageSkipButton));
			
		}
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.menu.getTrader().getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addRenderableWidget(new ItemTradeButton(this.leftPos + ItemTraderUtil.getButtonPosX(this.menu.getTrader(), i), this.topPos + ItemTraderUtil.getButtonPosY(this.menu.getTrader(), i), this::PressTradeButton, i, this, this.font, () -> this.menu.getTrader(), this.menu::GetCoinValue, this.menu::GetItemInventory)));
		}
	}
	
	@Override
	public void containerTick()
	{
		
		if(this.menu.getTrader() == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE) && !this.menu.isCashRegister();
		
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			this.buttonCollectMoney.visible = !this.menu.getTrader().getCoreSettings().hasBankAccount();
			this.buttonCollectMoney.active = this.menu.getTrader().getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.menu.getTrader().getCoreSettings().isCreative();
		}
		else
			this.buttonCollectMoney.visible = false;
		
		if(this.buttonSkipToPage != null)
		{
			this.buttonSkipToPage.active = this.getPageInput() >= 0 && this.getPageInput() < this.menu.getTotalCRSize() && this.getPageInput() != this.menu.getThisCRIndex();
		}
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		if(this.menu.getTrader() == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		IconAndButtonUtil.renderButtonTooltips(matrixStack, mouseX, mouseY, this.renderables);
		
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(matrixStack, this, this.menu.getTrader(), false, mouseX, mouseY);
		}
	}
	
	private void PressStorageButton(Button button)
	{
		//Open the container screen
		if(this.menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			//CurrencyMod.LOGGER.info("Owner attempted to open the Trader's Storage.");
			this.menu.getTrader().sendOpenStorageMessage();
		}
	}
	
	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
	}
	
	private void PressTradeButton(Button button)
	{
		
		int tradeIndex = 0;
		if(tradeButtons.contains(button))
			tradeIndex = tradeButtons.indexOf(button);
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
		
	}
	
	private void PressBackButton(Button button)
	{
		this.menu.player.closeContainer();
		this.minecraft.setScreen(new TradingTerminalScreen());
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
			return TextInputUtil.getIntegerValue(this.pageInput) - 1;
		}
		return 0;
	}
}
