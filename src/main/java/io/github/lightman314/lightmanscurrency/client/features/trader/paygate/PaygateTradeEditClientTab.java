package io.github.lightman314.lightmanscurrency.client.features.trader.paygate;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.IntParser;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeUnit;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.AdvancedTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.IDescriptionTrade;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.menu.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.nodes.PaygateTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.trade.PaygateTradeData;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.redstone.Redstone;

public class PaygateTradeEditClientTab extends AdvancedTradeEditClientTab<PaygateTradeEditTab> {

    public static final TabBuilder<TraderStorageMenu,PaygateTradeEditTab, TraderStorageTab,TraderStorageScreen> BUILDER = PaygateTradeEditClientTab::new;

    protected PaygateTradeEditClientTab(TraderStorageMenu menu, PaygateTradeEditTab commonTab, TraderStorageScreen screen) {
        super(menu, commonTab, screen);
    }

    private boolean tooltipEdit = false;

    private TextBoxWrapper<Integer> durationInput;

    private TextBoxWrapper<String> descriptionInput;
    private TextBoxWrapper<String> tooltipInput;

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        super.initialize(area, message);

        PaygateTradeData trade = null;
        if(this.getTrade() instanceof PaygateTradeData pt)
            trade = pt;

        this.durationInput = this.addChild(TextBoxWrapper.intBuilder()
                .parser(IntParser.builder().min(PaygateTradeData.DURATION_MIN).empty(1).max(PaygateTradeData::getMaxDuration).build())
                .atPos(area.pos.offset(15,50))
                .ofWidth(80)
                .withStartingValue(trade == null ? 20 : trade.getDuration())
                .withHandler(this.getCommonTab()::setDuration)
                .withOldWidget(this.durationInput)
                .visible(this::detailEditMode)
                .build());

        //+/- level buttons
        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(15,75))
                .withSprite(LCSprites.SIGN_PLUS)
                .active(this::canIncreaseLevel)
                .visible(this::detailEditMode)
                .onPress(this::addToLevel)
                .build());
        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(15,85))
                .withSprite(LCSprites.SIGN_MINUS)
                .active(this::canDecreaseLevel)
                .visible(this::detailEditMode)
                .onPress(this::removeFromLevel)
                .build());

        this.addChild(DirectionalSettingsWidget.builder()
                .atPos(area.pos.offset(area.width - 44,52))
                .forSettings(this::getPaygateTrade)
                .onPress(this::toggleState)
                .visible(this::detailEditMode)
                .build());

        this.descriptionInput = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(15,110))
                .ofWidth(area.width - 30)
                .withStartingValue(trade == null ? "" : trade.getDescription())
                .withHandler(this.getCommonTab()::setDescription)
                .withMaxLength(32)
                .visible(this::descriptionEditMode)
                .withOldWidget(this.descriptionInput)
                .build());

        this.tooltipInput = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(15,110))
                .ofWidth(area.width - 30)
                .withStartingValue(trade == null ? "" : trade.getTooltip())
                .withMaxLength(256)
                .withHandler(this.getCommonTab()::setTooltip)
                .visible(this::tooltipEditMode)
                .withOldWidget(this.tooltipInput)
                .build());

        //Mode Toggle
        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(7,110))
                .withSprite(LCSprites.TOGGLE.buildSprite(this::descriptionEditMode))
                .onPress(() -> this.tooltipEdit = !this.tooltipEdit)
                .visible(this::detailEditMode)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        super.extractBackground(gui, area);
        if(this.detailEditMode()) {
            PaygateTradeData trade = this.getPaygateTrade();

            //Duration Label
            gui.text(PaygateTradesNode.GUI_DURATION.get(),17,40,0xFF404040,false);
            //Duration Unit
            gui.text(TimeUnit.TICK.pluralText.get(),100,55,0xFF404040,false);

            //Power Level Label
            gui.text(PaygateTradesNode.GUI_REDSTONE_LEVEL.get(trade.getRedstoneLevel()),27,81,0xFF404040,false);

            //Description Label
            Component label = this.tooltipEdit ? IDescriptionTrade.GUI_TOOLTIP.get() : IDescriptionTrade.GUI_DESCRIPTION.get();
            gui.text(label,17,100,0xFF404040,false);

            //Output Side Label
            gui.centeredText(PaygateTradesNode.GUI_OUTPUT_SIDES.get(),area.width - 44,40,0xFF404040,false);
        }
    }

    private PaygateTradeData getPaygateTrade() {
        if(this.getTrade() instanceof PaygateTradeData trade)
            return trade;
        return new PaygateTradeData();
    }

    @Override
    protected void afterSelectionChange(TradeSlot oldSlot,TradeSlot newSlot) { }

    private boolean detailEditMode() { return this.getSelectedSlot().isOutput(); }
    private boolean descriptionEditMode() { return this.detailEditMode() && !this.tooltipEdit; }
    private boolean tooltipEditMode() { return this.detailEditMode() && this.tooltipEdit; }

    private int getCurrentLevel() {
        if(this.getTrade() instanceof PaygateTradeData trade)
            return trade.getRedstoneLevel();
        return 0;
    }
    private boolean canIncreaseLevel() { return this.getTrade() instanceof PaygateTradeData trade && trade.getRedstoneLevel() < Redstone.SIGNAL_MAX; }
    private boolean canDecreaseLevel() { return this.getTrade() instanceof PaygateTradeData trade && trade.getRedstoneLevel() > Redstone.SIGNAL_MIN; }

    private void addToLevel() { this.getCommonTab().setLevel(this.getCurrentLevel() + 1); }
    private void removeFromLevel() { this.getCommonTab().setLevel(this.getCurrentLevel() - 1); }

    private void toggleState(Direction side,boolean ignored) {
        DirectionalSettingsState newState = this.getPaygateTrade().getSidedState(side).allowsOutputs() ? DirectionalSettingsState.NONE : DirectionalSettingsState.OUTPUT;
        this.getCommonTab().setOutputSide(side,newState);
    }

}
