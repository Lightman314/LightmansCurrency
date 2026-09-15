package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.price.TradePriceWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeButton;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.AdvancedTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.rules.AbstractTradeRuleTab;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class AdvancedTradeEditClientTab<T extends AdvancedTradeEditTab> extends TraderStorageClientTab.Invisible<T> {

    public static final int PRICE_INPUT_Y = 40;

    protected TradeButton tradeDisplay;
    private TradePriceWidget priceInput;

    protected AdvancedTradeEditClientTab(TraderStorageMenu menu,T commonTab,TraderStorageScreen screen) {
        super(menu, commonTab, screen);
        this.getCommonTab().withSelectionListener(this::selectionChanged);
    }

    public final TradeSlot getSelectedSlot() { return this.getCommonTab().getSelectedSlot(); }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        //Render selection arrow
        if(this.tradeDisplay != null)
            this.tradeDisplay.renderSmallArrowAtSlot(gui,this.getCommonTab().getSelectedSlot());
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void initialize(ScreenArea area, FancyPacketMap message) {

        this.tradeDisplay = this.addChild(TradeButton.builder()
                .atPos(area.pos.offset(10,18))
                .withTrade(this.getCommonTab()::getSelectedTrade)
                .withContext(this.getMenu()::getTradeContext)
                .withInteractionHandler(this.getCommonTab())
                .build());

        this.priceInput = this.addChild(TradePriceWidget.builder()
                .atPos(area.pos.offset(0,PRICE_INPUT_Y))
                .ofWidth(area.width)
                .forTrade(this::getTrade)
                .withFilter(this::canPriceOptionBeValid)
                .withOptionDisplayFilter(this::isPriceOptionValid)
                .withHandler(this.getCommonTab())
                .withTypeChangeHandler(this.getCommonTab()::setTradePriceType)
                .withOldWidget(this.priceInput)
                .visible(this::isCurrentlyPriceSlot)
                .build());

        //Add Trade Rule Button
        if(this.getTrade() instanceof TradeRuleHolder holder) {
            this.addRightEdgeWidget(IconButton.builder()
                    .withIcon(ItemIcon.of(Items.BOOK))
                    .onPress(this.getCommonTab()::openTradeRuleTab)
                    .visible(() -> this.getTrade() instanceof TradeRuleHolder && this.getPermission(BuiltInPermissions.EDIT_TRADE_RULES))
                    .tooltip(TooltipSource.simple(AbstractTradeRuleTab.TOOLTIP_TRADER_TRADE_RULES_TRADE))
                    .build());
        }

        //Inform the price input about the currently selected slot
        if(this.isCurrentlyPriceSlot())
            this.priceInput.refactorSlot(this.getSelectedSlot().slot());

    }

    @Nullable
    protected final TradeData getTrade() { return this.getCommonTab().getSelectedTrade(); }

    protected final TradeDirection getTradeDirection() {
        TradeData trade = this.getTrade();
        return trade == null ? TradeDirection.OTHER : trade.getDirection();
    }

    private boolean canPriceOptionBeValid(TradePriceType<?> type) {
        TraderData trader = this.getTrader();
        TradeData trade = this.getTrade();
        if(trader != null && trade != null)
            return type.create().maySupportTrade(trader,trade);
        return false;
    }

    private boolean isPriceOptionValid(TradePriceType<?> type) {
        TraderData trader = this.getTrader();
        TradeData trade = this.getTrade();
        if(trader != null && trade != null)
            return type.create().currentlySupportsTrade(trader,trade);
        return false;
    }

    private void selectionChanged(TradeSlot oldSlot,TradeSlot newSlot) {
        this.afterSelectionChange(oldSlot,newSlot);
        if(this.isPriceSlot(newSlot) && this.priceInput != null)
            this.priceInput.refactorSlot(newSlot.slot());
    }

    protected final void invertSelectedSlot() {
        TradeSlot slot = this.getSelectedSlot();
        if(slot.isInputOrOutput())
            this.getCommonTab().changeSelection(slot.flipped());
    }

    protected boolean isCurrentlyPriceSlot() { return this.isPriceSlot(this.getSelectedSlot()); }

    protected boolean isPriceSlot(TradeSlot slot) {
        TradeData trade = this.getTrade();
        if(trade == null)
            return false;
        return slot.isPriceSlot(trade.getDirection());
    }

    @Override
    public boolean blockInventoryButtonClosing() { return true; }

    protected abstract void afterSelectionChange(TradeSlot oldSlot, TradeSlot newSlot);

    @Override
    @OverridingMethodsMustInvokeSuper
    public void handleMessage(FancyPacketMap message) {
        super.handleMessage(message);
        if(message.contains("price_type_flag") && this.priceInput != null)
            this.priceInput.updateHandlerAfterTypeChange();
    }

}
