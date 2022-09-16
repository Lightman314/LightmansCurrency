package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.paygate;

import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.paygate.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.paygate.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PaygateTradeEditClientTab extends TraderStorageClientTab<PaygateTradeEditTab> implements InteractionConsumer {
	
	public PaygateTradeEditClientTab(TraderStorageScreen screen, PaygateTradeEditTab commonTab) {
		super(screen, commonTab); 
	}
	
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

	@Override
	public MutableComponent getTooltip() { return Component.empty(); }

	@Override
	public boolean tabButtonVisible() { return false; }
	
	@Override
	public boolean blockInventoryClosing() { return true; }
	
	@Override
	public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

	TradeButton tradeDisplay;
	CoinValueInput priceSelection;
	EditBox durationInput;
	
	@Override
	public void onOpen() {
		
		PaygateTradeData trade = this.commonTab.getTrade();
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getContext, this.commonTab::getTrade, button -> {}));
		this.tradeDisplay.move(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 18);
		this.priceSelection = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft() + TraderScreen.WIDTH / 2 - CoinValueInput.DISPLAY_WIDTH / 2, this.screen.getGuiTop() + 55, Component.empty(), trade == null ? CoinValue.EMPTY : trade.getCost(), this.font, this::onValueChanged, this.screen::addRenderableTabWidget));
		this.priceSelection.drawBG = false;
		this.priceSelection.init();
		
		int labelWidth = this.font.width(Component.translatable("gui.lightmanscurrency.duration"));
		int unitWidth = this.font.width(Component.translatable("gui.lightmanscurrency.duration.unit"));
		this.durationInput = this.screen.addRenderableTabWidget(new EditBox(this.font, this.screen.getGuiLeft() + 15 + labelWidth, this.screen.getGuiTop() + 38, this.screen.getXSize() - 30 - labelWidth - unitWidth, 18, Component.empty()));
		this.durationInput.setValue(String.valueOf(trade.getDuration()));
		
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.commonTab.getTrade() == null)
			return;
		
		this.validateRenderables();
		
		this.font.draw(pose, Component.translatable("gui.lightmanscurrency.duration"), this.screen.getGuiLeft() + 13, this.screen.getGuiTop() + 42, 0x404040);
		int unitWidth = this.font.width(Component.translatable("gui.lightmanscurrency.duration.unit"));
		this.font.draw(pose, Component.translatable("gui.lightmanscurrency.duration.unit"), this.screen.getGuiLeft() + this.screen.getXSize() - unitWidth - 13, this.screen.getGuiTop() + 42, 0x404040);
		
		
	}
	
	private void validateRenderables() {
		
		this.priceSelection.visible = !this.commonTab.getTrade().isTicketTrade();
		if(this.priceSelection.visible)
			this.priceSelection.tick();
		TextInputUtil.whitelistInteger(this.durationInput, PaygateTraderData.DURATION_MIN, PaygateTraderData.DURATION_MAX);
		int inputDuration = Math.max(TextInputUtil.getIntegerValue(this.durationInput, PaygateTraderData.DURATION_MIN), PaygateTraderData.DURATION_MIN);
		if(inputDuration != this.commonTab.getTrade().getDuration())
			this.commonTab.setDuration(inputDuration);
	}
	
	@Override
	public void tick() {
		this.durationInput.tick();
	}

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		this.tradeDisplay.renderTooltips(pose, mouseX, mouseY);
		
	}
	
	@Override
	public void receiveSelfMessage(CompoundTag message) {
		if(message.contains("TradeIndex"))
			this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
	}

	@Override
	public void onTradeButtonInputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
		if(trade instanceof PaygateTradeData)
		{
			PaygateTradeData t = (PaygateTradeData)trade;
			if(this.menu.getCarried().getItem() == ModItems.TICKET_MASTER.get())
			{
				UUID ticketID = TicketItem.GetTicketID(this.menu.getCarried());
				this.commonTab.setTicket(ticketID);
				return;
			}
			else if(t.isTicketTrade())
			{
				this.commonTab.setTicket(null);
				return;
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.onInteractionClick((int)mouseX, (int)mouseY, button, this);
		return false;
	}
	
	@Override
	public void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) { }

	@Override
	public void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton) { }

	public void onValueChanged(CoinValue value) { this.commonTab.setPrice(value.copy()); }
	
}
