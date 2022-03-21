package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TradeItemPriceScreen extends Screen implements ICoinValueInput{
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tradeprice.png");
	
	private int xSize = 176;
	private int ySize = 88 + CoinValueInput.HEIGHT;
	
	PlayerEntity player;
	Supplier<IItemTrader> traderSource;
	public IItemTrader getTrader() { return this.traderSource.get(); }
	ItemTradeData trade;
	int tradeIndex;
	
	Button buttonSetSell;
	Button buttonSetPurchase;
	Button buttonSetBarter;
	
	Button buttonTradeRules;
	
	//Button buttonSetFree;
	
	ItemTradeType localDirection;
	
	CoinValueInput priceInput;
	
	TextFieldWidget nameField;
	
	public TradeItemPriceScreen(Supplier<IItemTrader> traderSource, int tradeIndex, PlayerEntity player)
	{
		super(new TranslationTextComponent("gui.lightmanscurrency.changeprice"));
		this.traderSource = traderSource;
		this.tradeIndex = tradeIndex;
		this.trade = this.getTrader().getTrade(this.tradeIndex);
		this.player = player;
		this.localDirection = this.trade.getTradeType();
	}
	
	@Override
	protected void init()
	{
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		this.priceInput = new CoinValueInput(guiTop, this.title, this.trade.getCost(), this);
		this.children.add(this.priceInput);
		
		this.nameField = new TextFieldWidget(this.font, guiLeft + 8, guiTop + CoinValueInput.HEIGHT + 38, 160, 18, ITextComponent.getTextComponentOrEmpty(""));
		this.nameField.setText(this.trade.getCustomName());
		this.nameField.setMaxStringLength(ItemTradeData.MAX_CUSTOMNAME_LENGTH);
		this.children.add(this.nameField);
		
		this.buttonSetSell = this.addButton(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 6, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.tradedirection.sale"), this::SetTradeDirection));
		this.buttonSetPurchase = this.addButton(new Button(guiLeft + 63, guiTop + CoinValueInput.HEIGHT + 6, 51, 20, new TranslationTextComponent("gui.button.lightmanscurrency.tradedirection.purchase"), this::SetTradeDirection));
		this.buttonSetBarter = this.addButton(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 6, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.tradedirection.barter"), this::SetTradeDirection));
		
		this.addButton(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.save"), this::PressSaveButton));
		this.addButton(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.back"), this::PressBackButton));
		this.buttonTradeRules = this.addButton(IconAndButtonUtil.tradeRuleButton(guiLeft + this.xSize, guiTop + CoinValueInput.HEIGHT, this::PressTradeRuleButton));
		this.buttonTradeRules.visible = this.getTrader().hasPermission(this.player, Permissions.EDIT_TRADE_RULES);
		
		tick();
		
	}
	
	@Override
	public void tick()
	{
		if(this.getTrader() == null)
		{
			this.player.closeScreen();
			return;
		}
		this.buttonSetSell.active = this.localDirection != ItemTradeType.SALE;
		this.buttonSetPurchase.active = this.localDirection != ItemTradeType.PURCHASE;
		this.buttonSetBarter.active = this.localDirection != ItemTradeType.BARTER;
		
		this.buttonTradeRules.visible = this.getTrader().hasPermission(this.player, Permissions.EDIT_TRADE_RULES);
		
		this.priceInput.visible = this.localDirection != ItemTradeType.BARTER;
		
		super.tick();
		this.priceInput.tick();
		this.nameField.tick();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(matrixStack, startX, startY + CoinValueInput.HEIGHT, 0, 0, this.xSize, this.ySize - CoinValueInput.HEIGHT);
		
		//Render the price input before rendering the buttons lest they get rendered behind it.
		this.priceInput.render(matrixStack, mouseX, mouseY, partialTicks);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		this.font.drawString(matrixStack, new TranslationTextComponent("gui.lightmanscurrency.customname").getString(), startX + 8.0F, startY + CoinValueInput.HEIGHT + 28.0F, 0x404040);
		
		this.nameField.render(matrixStack, mouseX, mouseY, partialTicks);
		
		if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		}
		
	}
	
	/*protected void PressFreeButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetItemPrice(tileEntity.getPos(), this.tradeIndex, new CoinValue(), true, this.nameField.getText(), this.localDirection.name()));
		PressBackButton(button);
	}*/
	
	protected void PressSaveButton(Button button)
	{
		SaveChanges();
		PressBackButton(button);
	}
	
	protected void SaveChanges()
	{
		this.getTrader().sendSetTradePriceMessage(this.tradeIndex, this.priceInput.getCoinValue(), this.nameField.getText(), this.localDirection);
	}
	
	protected void PressBackButton(Button button)
	{
		this.getTrader().sendOpenStorageMessage();
	}

	protected void SetTradeDirection(Button button)
	{
		if(button == buttonSetSell)
			this.localDirection = ItemTradeType.SALE;
		else if(button == buttonSetPurchase)
			this.localDirection = ItemTradeType.PURCHASE;
		else if(button == buttonSetBarter)
			this.localDirection = ItemTradeType.BARTER;
		else
			LightmansCurrency.LogWarning("Invalid button triggered SetTradeDirection");
	}
	
	protected void PressTradeRuleButton(Button button)
	{
		Minecraft.getInstance().displayGuiScreen(new TradeRuleScreen(getRuleScreenHandler()));
	}
	
	public ITradeRuleScreenHandler getRuleScreenHandler() { return new CloseRuleHandler(this.traderSource, this.tradeIndex, this.player); }
	
	private static class CloseRuleHandler implements ITradeRuleScreenHandler
	{

		final Supplier<IItemTrader> traderSource;
		private IItemTrader getTrader() { return this.traderSource.get(); }
		final int tradeIndex;
		final PlayerEntity player;
		
		public CloseRuleHandler(Supplier<IItemTrader> traderSource, int tradeIndex, PlayerEntity player)
		{
			this.traderSource = traderSource;
			this.tradeIndex = tradeIndex;
			this.player = player;
		}
		
		public ITradeRuleHandler ruleHandler() { return this.getTrader().getTrade(this.tradeIndex); }
		
		@Override
		public void reopenLastScreen() {
			Minecraft.getInstance().displayGuiScreen(new TradeItemPriceScreen(this.traderSource, this.tradeIndex, this.player));
		}
		
		public void updateServer(ResourceLocation type, CompoundNBT updateInfo)
		{
			this.getTrader().sendTradeRuleUpdateMessage(this.tradeIndex, type, updateInfo);
		}
		
		@Override
		public boolean stillValid() { return this.getTrader() != null; }
		
	}
	
	@Override
	public <T extends Button> T addCustomButton(T button) {
		return super.addButton(button);
	}
	
	@Override
	public <T extends IGuiEventListener> T addCustomListener(T listener) {
		return super.addListener(listener);
	}

	@Override
	public int getWidth() {
		return this.width;
	}
	
	@Override
	public void OnCoinValueChanged(CoinValueInput input) {
		
		//this.localPrice = input.getCoinValue();
	}

	@Override
	public FontRenderer getFont() {
		return this.font;
	}

}
