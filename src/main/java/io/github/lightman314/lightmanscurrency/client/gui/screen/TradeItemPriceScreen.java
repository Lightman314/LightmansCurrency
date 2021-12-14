package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageSetItemPrice;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageSetTraderRules;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
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

public class TradeItemPriceScreen extends Screen implements ICoinValueInput{
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tradeprice.png");
	
	private int xSize = 176;
	private int ySize = 88 + CoinValueInput.HEIGHT;
	
	Player player;
	ItemTraderBlockEntity tileEntity;
	ItemTradeData trade;
	int tradeIndex;
	
	Button buttonSetSell;
	Button buttonSetPurchase;
	Button buttonSetBarter;
	
	Button buttonTradeRules;
	
	ItemTradeType localDirection;
	
	CoinValueInput priceInput;
	
	EditBox nameField;
	
	public TradeItemPriceScreen(ItemTraderBlockEntity tileEntity, int tradeIndex, Player player)
	{
		super(new TranslatableComponent("gui.lightmanscurrency.changeprice"));
		this.tileEntity = tileEntity;
		this.tradeIndex = tradeIndex;
		this.trade = tileEntity.getTrade(this.tradeIndex);
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
		this.buttonTradeRules = this.addRenderableWidget(new IconButton(guiLeft + this.xSize, guiTop + CoinValueInput.HEIGHT, this::PressTradeRuleButton, GUI_TEXTURE, this.xSize, 0));
		
		tick();
		
	}
	
	@Override
	public void tick()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeContainer();
			return;
		}
		this.buttonSetSell.active = this.localDirection != ItemTradeType.SALE;
		this.buttonSetPurchase.active = this.localDirection != ItemTradeType.PURCHASE;
		this.buttonSetBarter.active = this.localDirection != ItemTradeType.BARTER;
		
		//this.buttonSetFree.active = this.localDirection != ItemTradeType.BARTER;
		
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
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetItemPrice(tileEntity.getBlockPos(), this.tradeIndex, this.priceInput.getCoinValue(), this.nameField.getValue(), this.localDirection.name()));
	}
	
	protected void PressBackButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(tileEntity.getBlockPos()));
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
	
	public ITradeRuleScreenHandler GetRuleScreenBackHandler() { return new CloseRuleHandler(this.tileEntity, this.tradeIndex, this.player); }
	
	private static class CloseRuleHandler implements ITradeRuleScreenHandler
	{

		final ItemTraderBlockEntity tileEntity;
		final int tradeIndex;
		final Player player;
		
		public CloseRuleHandler(ItemTraderBlockEntity tileEntity, int tradeIndex, Player player)
		{
			this.tileEntity = tileEntity;
			this.tradeIndex = tradeIndex;
			this.player = player;
		}
		
		public ITradeRuleHandler ruleHandler() { return this.tileEntity.getTrade(this.tradeIndex); }
		
		@Override
		public void reopenLastScreen() {
			Minecraft.getInstance().setScreen(new TradeItemPriceScreen(this.tileEntity, this.tradeIndex, this.player));
		}
		
		public void updateServer(List<TradeRule> newRules)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTraderRules(this.tileEntity.getBlockPos(), newRules, this.tradeIndex));
		}
		
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
