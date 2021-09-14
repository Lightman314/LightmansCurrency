package io.github.lightman314.lightmanscurrency.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageSetItemPrice;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.ItemTradeData;
import io.github.lightman314.lightmanscurrency.ItemTradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;

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
	
	TradeDirection localDirection;
	
	CoinValueInput priceInput;
	
	EditBox nameField;
	
	public TradeItemPriceScreen(ItemTraderBlockEntity tileEntity, int tradeIndex, Player player)
	{
		super(new TranslatableComponent("gui.lightmanscurrency.changeprice"));
		this.tileEntity = tileEntity;
		this.tradeIndex = tradeIndex;
		this.trade = tileEntity.getTrade(this.tradeIndex);
		this.player = player;
		this.localDirection = this.trade.getTradeDirection();
	}
	
	@Override
	protected void init()
	{
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		this.priceInput = this.addWidget(new CoinValueInput(guiTop, this.title, this.trade.getCost(), this));
		//this.children.add(this.priceInput);
		
		this.nameField = this.addRenderableWidget(new EditBox(this.font, guiLeft + 8, guiTop + CoinValueInput.HEIGHT + 38, 160, 18, TextComponent.EMPTY));
		this.nameField.setValue(this.trade.getCustomName());
		this.nameField.setMaxLength(ItemTradeData.MAX_CUSTOMNAME_LENGTH);
		//this.children.add(this.nameField);
		
		this.buttonSetSell = this.addRenderableWidget(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 6, 80, 20, new TranslatableComponent("gui.button.lightmanscurrency.tradedirection.sale"), this::SetTradeDirection));
		this.buttonSetPurchase = this.addRenderableWidget(new Button(guiLeft + 90, guiTop + CoinValueInput.HEIGHT + 6, 80, 20, new TranslatableComponent("gui.button.lightmanscurrency.tradedirection.purchase"), this::SetTradeDirection));
		
		this.addRenderableWidget(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.save"), this::SaveChanges));
		this.addRenderableWidget(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.back"), this::Back));
		this.addRenderableWidget(new Button(guiLeft + 63, guiTop + CoinValueInput.HEIGHT + 62, 51, 20, new TranslatableComponent("gui.button.lightmanscurrency.free"), this::SetFree));
		
		tick();
		
	}
	
	@Override
	public void tick()
	{
		if(this.tileEntity.isRemoved())
		{
			if(this.player instanceof LocalPlayer)
				((LocalPlayer)this.player).clientSideCloseContainer();
			return;
		}
		this.buttonSetSell.active = this.localDirection != TradeDirection.SALE;
		this.buttonSetPurchase.active = this.localDirection != TradeDirection.PURCHASE;
		
		super.tick();
		this.priceInput.tick();
		this.nameField.tick();
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		
		//RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		//this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		//RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(matrixStack, startX, startY + CoinValueInput.HEIGHT, 0, 0, this.xSize, this.ySize - CoinValueInput.HEIGHT);
		
		//Render the price input before rendering the buttons lest they get rendered behind it.
		this.priceInput.render(matrixStack, mouseX, mouseY, partialTicks);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		this.font.draw(matrixStack, new TranslatableComponent("gui.lightmanscurrency.customname").getString(), startX + 8.0F, startY + CoinValueInput.HEIGHT + 28.0F, 0x404040);
		
		this.nameField.render(matrixStack, mouseX, mouseY, partialTicks);
		
		
	}
	
	protected void SetFree(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetItemPrice(tileEntity.getBlockPos(), this.tradeIndex, new CoinValue(), true, this.nameField.getValue(), this.localDirection.name()));
		Back(button);
	}
	
	protected void SaveChanges(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetItemPrice(tileEntity.getBlockPos(), this.tradeIndex, this.priceInput.getCoinValue(), false, this.nameField.getValue(), this.localDirection.name()));
		Back(button);
	}
	
	protected void Back(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(tileEntity.getBlockPos()));
	}

	protected void SetTradeDirection(Button button)
	{
		if(button == buttonSetSell)
			this.localDirection = TradeDirection.SALE;
		else if(button == buttonSetPurchase)
			this.localDirection = TradeDirection.PURCHASE;
		else
			LightmansCurrency.LogWarning("Invalid button triggered SetTradeDirection");
	}
	
	@Override
	public <T extends AbstractWidget> T addCustomWidget(T widget) {
		return super.addRenderableWidget(widget);
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
	public Font getFont() {
		return this.font;
	}

}
