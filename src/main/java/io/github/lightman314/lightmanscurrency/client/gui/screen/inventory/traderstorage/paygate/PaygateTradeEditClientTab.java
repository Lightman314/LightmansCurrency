package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.paygate;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.settings.client.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.paygate.PaygateTradeEditTab;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Direction;
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
	EditBox descriptionInput;
	EditBox tooltipInput;

	IconButton ticketStubButton;

	private boolean descriptionEdit = true;
	private boolean priceEdit = true;
	private boolean isTicketTrade() {
		PaygateTradeData trade = this.commonTab.getTrade();
		return trade != null && trade.isTicketTrade();
	}
	private boolean priceEditMode() { return this.priceEdit && !this.isTicketTrade(); }
	private boolean detailEditMode() { return !this.priceEditMode(); }

	private boolean isDescriptionEdit() { return this.descriptionEdit; }
	private boolean isTooltipEdit() { return !this.descriptionEdit; }

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
				.position(screenArea.pos.offset(TraderScreen.WIDTH / 2 - MoneyValueWidget.WIDTH / 2, 40))
				.oldIfNotFirst(firstOpen,this.priceSelection)
				.startingValue(trade)
				.valueHandler(this::onValueChanged)
				.addon(EasyAddonHelper.visibleCheck(this::priceEditMode))
				.build());

		this.ticketStubButton = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(164,130))
				.pressAction(this::ToggleTicketStubHandling)
				.icon(this::GetTicketStubIcon)
				.addon(EasyAddonHelper.tooltip(this::getTicketStubButtonTooltip))
				.addon(EasyAddonHelper.visibleCheck(this::isTicketTrade))
				.build());

		this.durationInput = this.addChild(TextInputUtil.intBuilder()
				.position(screenArea.pos.offset(15,50))
				.size(80,18)
				.apply(IntParser.builder()
						.min(PaygateTraderData.DURATION_MIN)
						.max(PaygateTraderData::getMaxDuration)
						.empty(PaygateTraderData.DURATION_MIN)
						.consumer())
				.startingValue(trade == null ? 1 : trade.getDuration())
				.handler(this.commonTab::setDuration)
				.build());

		this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(15,75))
				.pressAction(this::increaseLevel)
				.sprite(IconAndButtonUtil.SPRITE_PLUS)
				.addon(EasyAddonHelper.activeCheck(this::canIncreaseLevel))
				.addon(EasyAddonHelper.visibleCheck(this::detailEditMode))
				.build());
		this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(15,85))
				.pressAction(this::decreaseLevel)
				.sprite(IconAndButtonUtil.SPRITE_MINUS)
				.addon(EasyAddonHelper.activeCheck(this::canDecreaseLevel))
				.addon(EasyAddonHelper.visibleCheck(this::detailEditMode))
				.build());

		this.addChild(DirectionalSettingsWidget.builder()
				.position(screenArea.pos.offset(screenArea.width - 44,52))
				.object(this.commonTab::getTrade)
				.handlers(this::ToggleOutputSide)
				.addon(EasyAddonHelper.visibleCheck(this::detailEditMode))
				.build());

		this.descriptionInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 15, screenArea.y + 110, screenArea.width - 30, 18, EasyText.empty()));
		if(trade != null)
			this.descriptionInput.setValue(trade.getDescription());
		this.descriptionInput.setResponder(this.commonTab::setDescription);
		this.descriptionInput.visible = this.isDescriptionEdit();

		this.tooltipInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 15, screenArea.y + 110, screenArea.width - 30, 18, EasyText.empty()));
		this.tooltipInput.setMaxLength(256);
		if(trade != null)
			this.tooltipInput.setValue(trade.getTooltip());
		this.tooltipInput.setResponder(this.commonTab::setTooltip);
		this.tooltipInput.visible = this.isTooltipEdit();

		this.addChild(PlainButton.builder()
				.position(screenArea.pos.offset(7,110))
				.sprite(IconAndButtonUtil.SPRITE_NEUTRAL_TOGGLE(this::isDescriptionEdit))
				.pressAction(() -> this.descriptionEdit = !this.descriptionEdit)
				.addon(EasyAddonHelper.visibleCheck(this::detailEditMode))
				.build());

	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		PaygateTradeData trade = this.commonTab.getTrade();
		if(trade == null)
			return;

		this.durationInput.visible = this.detailEditMode();
		this.descriptionInput.visible = this.detailEditMode() && this.isDescriptionEdit();
		this.tooltipInput.visible = this.detailEditMode() && this.isTooltipEdit();

		if(this.detailEditMode())
		{

			//Duration Label
			gui.drawString(LCText.GUI_TRADER_PAYGATE_DURATION.get(), 17, 40, 0x404040);
			//Duration Unit
			MutableComponent unitText = LCText.GUI_TRADER_PAYGATE_DURATION_UNIT.get();
			int unitWidth = gui.font.width(unitText);
			gui.drawString(unitText, 100, 55, 0x404040);

			//Power Level Label
			gui.drawString(LCText.GUI_TRADER_PAYGATE_LEVEL.get(trade.getRedstoneLevel()),27,81,0x404040);

			//Description Label
			Component label = this.isDescriptionEdit() ? LCText.GUI_TRADER_PAYGATE_DESCRIPTION.get() : LCText.GUI_TRADER_PAYGATE_TOOLTIP.get();
			gui.drawString(label,17, 100, 0x404040);

			//Output Side Label
			TextRenderUtil.drawCenteredText(gui,LCText.GUI_SETTINGS_OUTPUT_SIDE.get(), this.screen.getXSize() - 44, 40,0x404040);

		}

	}

	private Component getTicketStubButtonTooltip() {
		PaygateTradeData trade = this.commonTab.getTrade();
		if(trade != null)
			return trade.shouldStoreTicketStubs() ? LCText.TOOLTIP_TRADER_PAYGATE_TICKET_STUBS_KEEP.get() : LCText.TOOLTIP_TRADER_PAYGATE_TICKET_STUBS_GIVE.get();
		return null;
	}

	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.HandleInteractionClick((int)mouseX, (int)mouseY, button, this);
		return false;
	}

	@Override
	public void HandleTradeInputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int index) {
		if(trade instanceof PaygateTradeData t)
		{
			if(!this.priceEdit)
				this.priceEdit = true;
			else if(TicketItem.isMasterTicket(this.menu.getHeldItem()))
				this.commonTab.setTicket(this.menu.getHeldItem());
			else if(t.isTicketTrade())
				this.commonTab.setTicket(ItemStack.EMPTY);
		}
	}

	@Override
	public void HandleTradeOutputInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data, int index) { this.priceEdit = false; }

	@Override
	public void HandleOtherTradeInteraction(@Nonnull TraderData trader, @Nonnull TradeData trade, @Nonnull TradeInteractionData data) { }

	public void onValueChanged(MoneyValue value) { this.commonTab.setPrice(value); }

	private boolean canIncreaseLevel() {
		PaygateTradeData trade = this.commonTab.getTrade();
		return trade != null && trade.getRedstoneLevel() < 15;
	}

	private boolean canDecreaseLevel() {
		PaygateTradeData trade = this.commonTab.getTrade();
		return trade != null && trade.getRedstoneLevel() > 1;
	}

	private void increaseLevel() {
		PaygateTradeData trade = this.commonTab.getTrade();
		if(trade != null)
			this.commonTab.setLevel(trade.getRedstoneLevel() + 1);
	}

	private void decreaseLevel() {
		PaygateTradeData trade = this.commonTab.getTrade();
		if(trade != null)
			this.commonTab.setLevel(trade.getRedstoneLevel() - 1);
	}

	@Override
	protected void OpenMessage(@Nonnull LazyPacketData clientData) {
		if(clientData.contains("PriceEdit"))
			this.priceEdit = clientData.getBoolean("PriceEdit");
	}

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

	private void ToggleOutputSide(Direction side, boolean inverse)
	{
		PaygateTradeData trade = this.commonTab.getTrade();
		if(trade != null)
		{
			DirectionalSettingsState state = trade.getSidedState(side);
			this.commonTab.setOutputSide(side,state.getNext(trade));
		}
	}

}