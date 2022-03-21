package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class TradeItemPriceScreen extends Screen implements ICoinValueInput{
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tradeprice.png");
	
	private int xSize = 176;
	private int ySize = 88 + CoinValueInput.HEIGHT;
	
	Player player;
	private Supplier<IItemTrader> traderSource;
	public IItemTrader getTrader() { return this.traderSource.get(); }
	ItemTradeData trade;
	int tradeIndex;
	
	Button buttonSetSell;
	Button buttonSetPurchase;
	Button buttonSetBarter;
	
	Button buttonTradeRules;
	
	ItemTradeType localDirection;
	
	CoinValueInput priceInput;
	
	EditBox nameField;
	
	public TradeItemPriceScreen(Supplier<IItemTrader> traderSource, int tradeIndex, Player player)
	{
		super(new TranslatableComponent("gui.lightmanscurrency.changeprice"));
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
		
		this.priceInput = this.addRenderableWidget(new CoinValueInput(guiTop, this.title, this.trade.getCost(), this));
		this.priceInput.init();
		
		this.nameField = this.addRenderableWidget(new EditBox(this.font, guiLeft + 8, guiTop + CoinValueInput.HEIGHT + 38, 160, 18, new TextComponent("")));
		this.nameField.setValue(this.trade.getCustomName());
		this.nameField.setMaxLength(ItemTradeData.MAX_CUSTOMNAME_LENGTH);
		
		this.buttonSetSell = this.addRenderableWidget(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 6, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.tradedirection.sale"), this::SetTradeDirection));
		this.buttonSetPurchase = this.addRenderableWidget(new Button(guiLeft + 63, guiTop + CoinValueInput.HEIGHT + 6, 51, 20, new TranslatableComponent("gui.button.lightmanscurrency.tradedirection.purchase"), this::SetTradeDirection));
		this.buttonSetBarter = this.addRenderableWidget(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 6, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.tradedirection.barter"), this::SetTradeDirection));
		
		this.addRenderableWidget(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.save"), this::PressSaveButton));
		this.addRenderableWidget(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.back"), this::PressBackButton));
		//this.buttonSetFree = this.addRenderableWidget(new Button(guiLeft + 63, guiTop + CoinValueInput.HEIGHT + 62, 51, 20, new TranslatableComponent("gui.button.lightmanscurrency.free"), this::PressFreeButton));
		this.buttonTradeRules = this.addRenderableWidget(IconAndButtonUtil.tradeRuleButton(guiLeft + this.xSize, guiTop + CoinValueInput.HEIGHT, this::PressTradeRuleButton));
		this.buttonTradeRules.visible = this.getTrader().hasPermission(this.player, Permissions.EDIT_TRADE_RULES);
		
		tick();
		
	}
	
	@Override
	public void tick()
	{
		if(this.getTrader() == null)
		{
			this.player.closeContainer();
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
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(poseStack);
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(poseStack, startX, startY + CoinValueInput.HEIGHT, 0, 0, this.xSize, this.ySize - CoinValueInput.HEIGHT);
		
		super.render(poseStack, mouseX, mouseY, partialTicks);
		
		this.font.draw(poseStack, new TranslatableComponent("gui.lightmanscurrency.customname").getString(), startX + 8.0F, startY + CoinValueInput.HEIGHT + 28.0F, 0x404040);
		
		if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		}
		
	}
	
	protected void PressSaveButton(Button button)
	{
		SaveChanges();
		PressBackButton(button);
	}
	
	protected void SaveChanges()
	{
		this.getTrader().sendSetTradePriceMessage(this.tradeIndex, this.priceInput.getCoinValue(), this.nameField.getValue(), this.localDirection);
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
		Minecraft.getInstance().setScreen(new TradeRuleScreen(getRuleScreenHandler()));
	}
	
	public ITradeRuleScreenHandler getRuleScreenHandler() { return new CloseRuleHandler(this.traderSource, this.tradeIndex, this.player); }
	
	private static class CloseRuleHandler implements ITradeRuleScreenHandler
	{

		final Supplier<IItemTrader> traderSource;
		private IItemTrader getTrader() { return this.traderSource.get(); }
		final int tradeIndex;
		final Player player;
		
		public CloseRuleHandler(Supplier<IItemTrader> traderSource, int tradeIndex, Player player)
		{
			this.traderSource = traderSource;
			this.tradeIndex = tradeIndex;
			this.player = player;
		}
		
		public ITradeRuleHandler ruleHandler() { return this.getTrader().getTrade(this.tradeIndex); }
		
		@Override
		public void reopenLastScreen() {
			Minecraft.getInstance().setScreen(new TradeItemPriceScreen(this.traderSource, this.tradeIndex, this.player));
		}
		
		@Override
		public void updateServer(ResourceLocation type, CompoundTag updateInfo)
		{
			this.getTrader().sendTradeRuleUpdateMessage(this.tradeIndex, type, updateInfo);
		}
		
		@Override
		public boolean stillValid() { return this.getTrader() != null; }
		
	}
	
	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T button) {
		return this.addRenderableWidget(button);
	}

	@Override
	public int getWidth() {
		return this.width;
	}
	
	@Override
	public void OnCoinValueChanged(CoinValueInput input) {
		
	}

	@Override
	public Font getFont() {
		return this.font;
	}

}
