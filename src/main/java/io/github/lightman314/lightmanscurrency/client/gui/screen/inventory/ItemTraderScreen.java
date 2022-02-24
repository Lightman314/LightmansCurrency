package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.*;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainer;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRNextTrader;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRSkipTo;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

@IPNIgnore
public class ItemTraderScreen extends ContainerScreen<ItemTraderContainer>{

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
	
	TextFieldWidget pageInput;
	Button buttonSkipToPage;
	
	protected List<ItemTradeButton> tradeButtons = new ArrayList<>();
	
	public ItemTraderScreen(ItemTraderContainer container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.ySize = 133 + ItemTraderUtil.getTradeDisplayHeight(this.container.getTrader());
		this.xSize = ItemTraderUtil.getWidth(this.container.getTrader());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		if(this.container.getTrader() == null)
			return;
		
		//int tradeCount = trader.getTradeCount();
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - xSize) / 2;
		int startY = (this.height - ySize) / 2;
		
		int columnCount = ItemTraderUtil.getTradeDisplayColumnCount(this.container.getTrader());
		int rowCount = ItemTraderUtil.getTradeDisplayRowCount(this.container.getTrader());
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.container.getTrader());
				
		//Top-left corner
		this.blit(matrix, startX + ItemTraderUtil.getTradeDisplayOffset(this.container.getTrader()), startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			this.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, ItemTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				this.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY, 6 + ItemTradeButton.WIDTH, 0, TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		this.blit(matrix, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(this.container.getTrader()) - 6, startY, 91, 0, 6, 17);
		
		//Draw the bg & spacer of each button
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			this.blit(matrix, startX + tradeOffset, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 0, 17, 6, TRADEBUTTON_VERTICALITY);
			for(int x = 0; x < columnCount; x++)
			{
				//Button BG
				this.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6, 17, ItemTradeButton.WIDTH, TRADEBUTTON_VERTICALITY);
				//Right spacer for the trade button
				if(x < columnCount - 1)
					this.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6 + ItemTradeButton.WIDTH, 17, TRADEBUTTON_HORIZ_SPACER, TRADEBUTTON_VERTICALITY);
			}
			//Right edge
			this.blit(matrix, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(this.container.getTrader()) - 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 91, 17, 6, TRADEBUTTON_VERTICALITY);
		}
		
		//Bottom-left corner
		this.blit(matrix, startX + tradeOffset, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 0, 43, 6, 7);
		for(int x = 0; x < columnCount; x++)
		{
			//Bottom of each button
			this.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 43, ItemTradeButton.WIDTH, 7);
			//Bottom spacer of each button
			if(x < columnCount - 1)
				this.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + ItemTradeButton.WIDTH + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 43, TRADEBUTTON_HORIZ_SPACER, 7);
		}
		//Bottom-right corner
		this.blit(matrix, startX + tradeOffset + ItemTraderUtil.getTradeDisplayWidth(this.container.getTrader()) - 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 91, 43, 6, 7);
		
		//Draw the bottom (player inventory/coin slots)
		this.blit(matrix, startX + ItemTraderUtil.getInventoryDisplayOffset(this.container.getTrader()), startY + ItemTraderUtil.getTradeDisplayHeight(this.container.getTrader()), 0, 50, 176, 133);
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		if(this.container.getTrader() == null)
			return;
		
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.container.getTrader());
		int inventoryOffset = ItemTraderUtil.getInventoryDisplayOffset(this.container.getTrader());
		
		font.drawString(matrix, this.container.getTrader().getTitle().getString(), tradeOffset + 8.0f, 6.0f, 0x404040);
		
		font.drawString(matrix, this.playerInventory.getName().getString(), inventoryOffset + 8.0f, (ySize - 94), 0x404040);
		
		font.drawString(matrix, new TranslationTextComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.container.GetCoinValue())).getString(), inventoryOffset + 80f, ySize - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.container.getTrader());
		int inventoryOffset = ItemTraderUtil.getInventoryDisplayOffset(this.container.getTrader());
		
		this.buttonShowStorage = this.addButton(IconAndButtonUtil.storageButton(this.guiLeft - 20 + tradeOffset, this.guiTop, this::PressStorageButton));
		this.buttonShowStorage.visible = this.container.hasPermission(Permissions.OPEN_STORAGE) && !this.container.isCashRegister();
		
		this.buttonCollectMoney = this.addButton(IconAndButtonUtil.collectCoinButton(this.guiLeft - 20 + tradeOffset, this.guiTop + 20, this::PressCollectionButton, this.container::getTrader));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.container.hasPermission(Permissions.COLLECT_COINS) && !this.container.getTrader().getCoreSettings().hasBankAccount();
		
		if(this.container.isUniversal())
			this.buttonBack = this.addButton(IconAndButtonUtil.backToTerminalButton(this.guiLeft - 20 + inventoryOffset, this.guiTop + this.ySize - 20, this::PressBackButton));
		
		if(this.container.isCashRegister() && this.container.getTotalCRSize() > 1)
		{
			this.buttonLeft = this.addButton(IconAndButtonUtil.leftButton(this.guiLeft + tradeOffset - 20, this.guiTop, this::PressArrowButton));
			this.buttonRight = this.addButton(IconAndButtonUtil.rightButton(this.guiLeft + this.xSize - tradeOffset, this.guiTop, this::PressArrowButton));
			
			this.pageInput = this.addButton(new TextFieldWidget(this.font, this.guiLeft + 50, this.guiTop - 19, this.xSize - 120, 18, new StringTextComponent("")));
			this.pageInput.setMaxStringLength(9);
			this.pageInput.setText(String.valueOf(this.container.getThisCRIndex() + 1));
			
			this.buttonSkipToPage = this.addButton(IconAndButtonUtil.rightButton(this.guiLeft + this.xSize - 68,  this.guiTop - 20, this::PressPageSkipButton));
			
		}
		
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.container.getTrader().getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addButton(new ItemTradeButton(this.guiLeft + ItemTraderUtil.getButtonPosX(this.container.getTrader(), i), this.guiTop + ItemTraderUtil.getButtonPosY(this.container.getTrader(), i), this::PressTradeButton, i, this, this.font, () -> this.container.getTrader(), () -> this.container.GetCoinValue(), () -> this.container.GetItemInventory())));
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
		
		super.tick();
		
		this.buttonShowStorage.visible = this.container.hasPermission(Permissions.OPEN_STORAGE);
		if(this.container.hasPermission(Permissions.COLLECT_COINS))
		{
			this.buttonCollectMoney.visible = !this.container.getTrader().getCoreSettings().hasBankAccount();
			this.buttonCollectMoney.active = this.container.getTrader().getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.container.getTrader().getCoreSettings().isCreative();
		}
		else
			this.buttonCollectMoney.visible = false;
		
		if(this.buttonSkipToPage != null)
		{
			this.buttonSkipToPage.active = this.getPageInput() >= 0 && this.getPageInput() < this.container.getTotalCRSize() && this.getPageInput() != this.container.getThisCRIndex();
		}
		
		
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
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX,  mouseY);
		
		IconAndButtonUtil.renderButtonTooltips(matrixStack, mouseX, mouseY, this.buttons);
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(matrixStack, this, this.container.getTrader(), false, mouseX, mouseY);
		}
	}
	
	private void PressStorageButton(Button button)
	{
		//Open the container screen
		if(this.container.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.container.getTrader().sendOpenStorageMessage();
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
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
		
	}
	
	private void PressBackButton(Button button)
	{
		this.container.player.closeScreen();
		this.minecraft.displayGuiScreen(new TradingTerminalScreen());
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
