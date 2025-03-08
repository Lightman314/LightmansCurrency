package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.paygate;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.paygate.PaygateTradeEditTab;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class PaygateTradeEditClientTab extends TraderStorageClientTab<PaygateTradeEditTab> implements TradeInteractionHandler, IMouseListener {
	
	public PaygateTradeEditClientTab(Object screen, PaygateTradeEditTab commonTab) { super(screen, commonTab); }
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

	@Override
	public MutableComponent getTooltip() { return EasyText.empty(); }

	@Override
	public boolean tabVisible() { return false; }
	
	@Override
	public boolean blockInventoryClosing() { return true; }
	
	@Override
	public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

	TradeButton tradeDisplay;
	MoneyValueWidget priceSelection;
	EditBox durationInput;

	IconButton ticketStubButton;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		PaygateTradeData trade = this.commonTab.getTrade();
		
		this.tradeDisplay = this.addChild(TradeButton.builder()
				.position(screenArea.pos.offset(10,18))
				.context(this.menu::getContext)
				.trade(this.commonTab::getTrade)
				.build());
		this.tradeDisplay.setPosition(screenArea.pos.offset(10, 18));
		this.priceSelection = this.addChild(MoneyValueWidget.builder()
				.position(screenArea.pos.offset(TraderScreen.WIDTH / 2 - MoneyValueWidget.WIDTH / 2, 55))
				.oldIfNotFirst(firstOpen,this.priceSelection)
				.startingValue(trade)
				.valueHandler(this::onValueChanged)
				.addon(EasyAddonHelper.visibleCheck(() -> { PaygateTradeData t = this.commonTab.getTrade(); return t != null && !t.isTicketTrade(); }))
				.build());

		this.ticketStubButton = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(10,55))
				.pressAction(this::ToggleTicketStubHandling)
				.icon(this::GetTicketStubIcon)
				.addon(EasyAddonHelper.tooltip(this::getTicketStubButtonTooltip))
				.addon(EasyAddonHelper.visibleCheck(() -> { PaygateTradeData t = this.commonTab.getTrade(); return t != null && t.isTicketTrade(); }))
				.build());
		
		int labelWidth = this.getFont().width(LCText.GUI_TRADER_PAYGATE_DURATION.get());
		int unitWidth = this.getFont().width(LCText.GUI_TRADER_PAYGATE_DURATION_UNIT.get());
		this.durationInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 15 + labelWidth, screenArea.y + 38, screenArea.width - 30 - labelWidth - unitWidth, 18, EasyText.empty()));
		this.durationInput.setValue(String.valueOf(trade.getDuration()));

	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		if(this.commonTab.getTrade() == null)
			return;

		//Whitelist and update the input duration value
		TextInputUtil.whitelistInteger(this.durationInput, PaygateTraderData.DURATION_MIN, PaygateTraderData.getMaxDuration());
		int inputDuration = Math.max(TextInputUtil.getIntegerValue(this.durationInput, PaygateTraderData.DURATION_MIN), PaygateTraderData.DURATION_MIN);
		if(inputDuration != this.commonTab.getTrade().getDuration())
			this.commonTab.setDuration(inputDuration);

		gui.drawString(LCText.GUI_TRADER_PAYGATE_DURATION.get(), 13, 42, 0x404040);
		MutableComponent unitText = LCText.GUI_TRADER_PAYGATE_DURATION_UNIT.get();
		int unitWidth = gui.font.width(unitText);
		gui.drawString(unitText, this.screen.getXSize() - unitWidth - 13, 42, 0x404040);
		
	}

	private Component getTicketStubButtonTooltip() {
		PaygateTradeData trade = this.commonTab.getTrade();
		if(trade != null)
			return trade.shouldStoreTicketStubs() ? LCText.TOOLTIP_TRADER_PAYGATE_TICKET_STUBS_KEEP.get() : LCText.TOOLTIP_TRADER_PAYGATE_TICKET_STUBS_GIVE.get();
		return null;
	}

	@Override
	public void HandleTradeInputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int index) {
		if(trade instanceof PaygateTradeData t)
		{
			if(TicketItem.isMasterTicket(this.menu.getHeldItem()))
				this.commonTab.setTicket(this.menu.getHeldItem());
			else if(t.isTicketTrade())
				this.commonTab.setTicket(ItemStack.EMPTY);
		}
	}
	
	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.HandleInteractionClick((int)mouseX, (int)mouseY, button, this);
		return false;
	}

	@Override
	public void HandleTradeOutputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int index) { }

	@Override
	public void HandleOtherTradeInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data) { }

	public void onValueChanged(MoneyValue value) { this.commonTab.setPrice(value); }

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
