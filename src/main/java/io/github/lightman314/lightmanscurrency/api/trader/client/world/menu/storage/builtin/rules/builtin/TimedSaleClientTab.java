package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.IntParser;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeData;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeUnit;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.RuleClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.TimedSale;
import net.minecraft.network.chat.Component;

public class TimedSaleClientTab extends RuleClientTab<TimedSale> {

    public TimedSaleClientTab(TradeRulesClientTab tab) { super(tab); }

    @Override
    public TradeRuleType<TimedSale> getType() { return TimedSale.TYPE; }

    private TextBoxWrapper<Integer> discountInput = null;

    @Override
    protected void initialize(ScreenArea area) {

        TimedSale rule = this.getRule();

        this.discountInput = this.addChild(TextBoxWrapper.intBuilder()
                .atPos(area.pos.offset(25,9))
                .ofWidth(30)
                .withMaxLength(3)
                .parser(IntParser.ONE_TO_ONE_HUNDRED)
                .withStartingValue(rule.getDiscount())
                .withHandler(this::onDiscountChanged)
                .withOldWidget(this.discountInput)
                .build());

        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(25,45))
                .ofWidth(area.width - 50)
                .withText(this::getButtonText)
                .onPress(this::pressStartButton)
                .tooltip(TooltipSource.deferredSingle(this::getButtonTooltip))
                .build());

        this.addChild(TimeInputWidget.builder()
                .atPos(area.pos.offset(63,75))
                .withUnitRange(TimeUnit.MINUTE,TimeUnit.DAY)
                .withDurationRange(TimeHelper.DURATION_MINUTE,TimeHelper.DURATION_DAY * 30)
                .withHandler(this::onTimeSet)
                .startTime(rule.getDuration())
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

        gui.push(this.discountInput.getPosition());
        gui.text(TimedSale.GUI_LABEL.get(),this.discountInput.getWidth() + 4,6,0xFF404040,false);
        gui.pop();

        Component infoText;
        if(this.getRule().timerActive())
            infoText = TimedSale.GUI_INFO_ACTIVE.get(this.getRule().getTimeRemaining().getShortString());
        else
            infoText = TimedSale.GUI_INFO_INACTIVE.get(new TimeData(this.getRule().getDuration()).getShortString());

        gui.centeredTextWithWordWrap(infoText,area.halfWidth(),35,area.width - 10,0xFF404040,false);

    }

    private Component getButtonText() {
        return this.getRule().timerActive() ? TimedSale.BUTTON_STOP.get() : TimedSale.BUTTON_START.get();
    }

    private Component getButtonTooltip() {
        return this.getRule().timerActive() ? TimedSale.TOOLTIP_STOP.get() : TimedSale.TOOLTIP_START.get();
    }

    private void onDiscountChanged(int newValue) {
        this.requestChange(FancyPacketMap.map().setInt("setDiscount",newValue));
    }

    private void pressStartButton() {
        this.requestChange(FancyPacketMap.map().setBoolean("setActive",!this.getRule().timerActive()));
    }

    private void onTimeSet(TimeData time) {
        this.requestChange(FancyPacketMap.map().setLong("setDuration",time.milliseconds));
    }

}
