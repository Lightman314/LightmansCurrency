package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TimeInputWidget;
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
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.PriceFluctuation;

public class PriceFluctuationClientTab extends RuleClientTab<PriceFluctuation> {

    public PriceFluctuationClientTab(TradeRulesClientTab tab) { super(tab); }

    @Override
    public TradeRuleType<PriceFluctuation> getType() { return PriceFluctuation.TYPE; }

    private TextBoxWrapper<Integer> fluctuationInput = null;

    @Override
    protected void initialize(ScreenArea area) {

        PriceFluctuation rule = this.getRule();

        this.fluctuationInput = this.addChild(TextBoxWrapper.intBuilder()
                .atPos(area.pos.offset(25,9))
                .ofWidth(30)
                .withMaxLength(3)
                .withStartingValue(rule.getFluctuation())
                .parser(IntParser.ONE_TO_ONE_HUNDRED)
                .withHandler(this::onFluctuationChanged)
                .withOldWidget(this.fluctuationInput)
                .build());

        this.addChild(TimeInputWidget.builder()
                .atPos(area.pos.offset(67,75))
                .withUnitRange(TimeUnit.MINUTE,TimeUnit.DAY)
                .minDuration(TimeHelper.DURATION_MINUTE)
                .withHandler(this::onTimeSet)
                .startTime(rule.getDuration())
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

        PriceFluctuation rule = this.getRule();

        gui.push(this.fluctuationInput.getPosition());
        gui.text(PriceFluctuation.GUI_LABEL.get(),this.fluctuationInput.getWidth() + 4,6,0xFF404040,false);
        gui.pop();

        gui.centeredTextWithWordWrap(PriceFluctuation.GUI_INFO.get(rule.getFluctuation(),new TimeData(rule.getDuration()).getShortString()),area.halfWidth(),35,area.width - 20,0xFF404040,false);

    }

    private void onFluctuationChanged(int newValue) {
        this.requestChange(FancyPacketMap.map().setInt("setFluctuation",newValue));
    }

    private void onTimeSet(TimeData newValue) {
        this.requestChange(FancyPacketMap.map().setLong("setDuration",newValue.milliseconds));
    }

}
