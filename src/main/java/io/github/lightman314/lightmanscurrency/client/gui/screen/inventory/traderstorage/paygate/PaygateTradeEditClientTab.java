package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.paygate;

import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.paygate.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class PaygateTradeEditClientTab extends TraderStorageClientTab<PaygateTradeEditTab> implements InteractionConsumer, IMouseListener {
	
	public PaygateTradeEditClientTab(Object screen, PaygateTradeEditTab commonTab) { super(screen, commonTab); }
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

	@Override
	public MutableComponent getTooltip() { return EasyText.empty(); }

	@Override
	public boolean tabButtonVisible() { return false; }
	
	@Override
	public boolean blockInventoryClosing() { return true; }
	
	@Override
	public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

	TradeButton tradeDisplay;
	CoinValueInput priceSelection;
	EditBox durationInput;

	IconButton ticketStubButton;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.addChild(this);

		PaygateTradeData trade = this.commonTab.getTrade();
		
		this.tradeDisplay = this.addChild(new TradeButton(this.menu::getContext, this.commonTab::getTrade, button -> {}));
		this.tradeDisplay.setPosition(screenArea.pos.offset(10, 18));
		this.priceSelection = this.addChild(new CoinValueInput(screenArea.pos.offset(TraderScreen.WIDTH / 2 - CoinValueInput.DISPLAY_WIDTH / 2, 55), EasyText.empty(), trade == null ? CoinValue.EMPTY : trade.getCost(), this.getFont(), this::onValueChanged)
				.withAddons(EasyAddonHelper.visibleCheck(() -> { PaygateTradeData t = this.commonTab.getTrade(); return t != null && !t.isTicketTrade(); })));
		this.priceSelection.drawBG = false;

		this.ticketStubButton = this.addChild(new IconButton(screenArea.pos.offset(10, 55), this::ToggleTicketStubHandling, this::GetTicketStubIcon)
				.withAddons(EasyAddonHelper.tooltip(this::getTicketStubButtonTooltip),
						EasyAddonHelper.visibleCheck(() -> { PaygateTradeData t = this.commonTab.getTrade(); return t != null && t.isTicketTrade(); })));
		
		int labelWidth = this.getFont().width(EasyText.translatable("gui.lightmanscurrency.duration"));
		int unitWidth = this.getFont().width(EasyText.translatable("gui.lightmanscurrency.duration.unit"));
		this.durationInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 15 + labelWidth, screenArea.y + 38, screenArea.width - 30 - labelWidth - unitWidth, 18, EasyText.empty()));
		this.durationInput.setValue(String.valueOf(trade.getDuration()));
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		if(this.commonTab.getTrade() == null)
			return;

		//Whitelist and update the input duration value
		TextInputUtil.whitelistInteger(this.durationInput, PaygateTraderData.DURATION_MIN, PaygateTraderData.DURATION_MAX);
		int inputDuration = Math.max(TextInputUtil.getIntegerValue(this.durationInput, PaygateTraderData.DURATION_MIN), PaygateTraderData.DURATION_MIN);
		if(inputDuration != this.commonTab.getTrade().getDuration())
			this.commonTab.setDuration(inputDuration);

		gui.drawString(EasyText.translatable("gui.lightmanscurrency.duration"), 13, 42, 0x404040);
		int unitWidth = this.getFont().width(EasyText.translatable("gui.lightmanscurrency.duration.unit"));
		gui.drawString(EasyText.translatable("gui.lightmanscurrency.duration.unit"), this.screen.getXSize() - unitWidth - 13, 42, 0x404040);
		
		
	}

	private Component getTicketStubButtonTooltip() {
		PaygateTradeData trade = this.commonTab.getTrade();
		if(trade != null)
			return trade.shouldStoreTicketStubs() ? EasyText.translatable("tooltip.lightmanscurrency.trader.paygate.store_stubs") : EasyText.translatable("tooltip.lightmanscurrency.trader.storage.dont_store_stubs");
		return null;
	}
	
	@Override
	public void receiveSelfMessage(CompoundTag message) {
		if(message.contains("TradeIndex"))
			this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
	}

	@Override
	public void onTradeButtonInputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
		if(trade instanceof PaygateTradeData t)
		{
			if(this.menu.getCarried().getItem() == ModItems.TICKET_MASTER.get())
				this.commonTab.setTicket(this.menu.getCarried());
			else if(t.isTicketTrade())
				this.commonTab.setTicket(ItemStack.EMPTY);
		}
	}
	
	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.onInteractionClick((int)mouseX, (int)mouseY, button, this);
		return false;
	}

	@Override
	public void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) { }

	@Override
	public void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton) { }

	public void onValueChanged(CoinValue value) { this.commonTab.setPrice(value); }

	private IconData GetTicketStubIcon()
	{
		PaygateTradeData trade = this.commonTab.getTrade();
		boolean shouldStore = trade != null && trade.shouldStoreTicketStubs();
		return shouldStore ? IconData.of(Items.CHEST) : IconData.of(Items.PLAYER_HEAD);
	}

	private void ToggleTicketStubHandling(EasyButton button)
	{
		PaygateTradeData trade = this.commonTab.getTrade();
		if(trade != null)
			this.commonTab.setTicketStubHandling(!trade.shouldStoreTicketStubs());
	}
	
}
