package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSetItemPrice2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSetTraderRules2;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class UniversalTradeItemPriceScreen extends Screen implements ICoinValueInput{
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tradeprice.png");
	
	private int xSize = 176;
	private int ySize = 88 + CoinValueInput.HEIGHT;
	
	Player player;
	ItemTradeData trade;
	int tradeIndex;
	UUID traderID;
	
	Button buttonSetSell;
	Button buttonSetPurchase;
	Button buttonSetBarter;
	
	Button buttonTradeRules;
	
	ItemTradeType localDirection;
	
	CoinValueInput priceInput;
	
	EditBox nameField;
	
	public UniversalTradeItemPriceScreen(UUID traderID, ItemTradeData tradeData, int tradeIndex, Player player)
	{
		super(new TranslatableComponent("gui.lightmanscurrency.changeprice"));
		this.player = player;
		this.traderID = traderID;
		this.trade = tradeData;
		this.tradeIndex = tradeIndex;
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
		
		this.addRenderableWidget(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.save"), this::SaveChanges));
		this.addRenderableWidget(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.back"), this::Back));
		//this.buttonSetFree = this.addRenderableWidget(new Button(guiLeft + 63, guiTop + CoinValueInput.HEIGHT + 62, 51, 20, new TranslatableComponent("gui.button.lightmanscurrency.free"), this::SetFree));
		this.buttonTradeRules = this.addRenderableWidget(new IconButton(guiLeft + this.xSize, guiTop + CoinValueInput.HEIGHT, this::PressTradeRuleButton, GUI_TEXTURE, this.xSize, 0));
		
		tick();
		
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		this.buttonSetSell.active = this.localDirection != ItemTradeType.SALE;
		this.buttonSetPurchase.active = this.localDirection != ItemTradeType.PURCHASE;
		this.buttonSetBarter.active = this.localDirection != ItemTradeType.BARTER;
		
		this.priceInput.visible = this.localDirection != ItemTradeType.BARTER;
		
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
		
		//Render the price input before rendering the buttons lest they get rendered behind it.
		//this.priceInput.render(poseStack, mouseX, mouseY, partialTicks);
		
		super.render(poseStack, mouseX, mouseY, partialTicks);
		
		this.font.draw(poseStack, new TranslatableComponent("gui.lightmanscurrency.customname").getString(), startX + 8.0F, startY + CoinValueInput.HEIGHT + 28.0F, 0x404040);
		
		if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		}
		
	}
	
	protected void SaveChanges(Button button)
	{
		SaveChanges();
		Back(button);
	}
	
	protected void SaveChanges()
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetItemPrice2(this.traderID, this.tradeIndex, this.priceInput.getCoinValue(), this.nameField.getValue(), this.localDirection.name()));
	}
	
	protected void Back(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(this.traderID));
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
		Minecraft.getInstance().setScreen(new TradeRuleScreen(GetRuleScreenBackHandler()));
	}
	
	public ITradeRuleScreenHandler GetRuleScreenBackHandler() { return new CloseRuleHandler(this.traderID, this.trade, this.tradeIndex, this.player); }
	
	private static class CloseRuleHandler implements ITradeRuleScreenHandler
	{

		final UUID traderID;
		final int tradeIndex;
		final Player player;
		final ItemTradeData tradeData;
		
		public CloseRuleHandler(UUID traderID, ItemTradeData tradeData, int tradeIndex, Player player)
		{
			this.traderID = traderID;
			this.tradeData = tradeData;
			this.tradeIndex = tradeIndex;
			this.player = player;
		}
		
		public ITradeRuleHandler ruleHandler() { return this.tradeData; }
		
		@Override
		public void reopenLastScreen() {
			Minecraft.getInstance().setScreen(new UniversalTradeItemPriceScreen(this.traderID, this.tradeData, this.tradeIndex, this.player));
		}
		
		public void updateServer(List<TradeRule> newRules)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTraderRules2(this.traderID, newRules, this.tradeIndex));
		}
		
	}
	
	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T widget) {
		return this.addRenderableWidget(widget);
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
