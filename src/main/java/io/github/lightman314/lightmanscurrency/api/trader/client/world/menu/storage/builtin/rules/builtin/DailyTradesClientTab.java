package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeData;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeUnit;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.RuleClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.DailyTrades;

public class DailyTradesClientTab extends RuleClientTab<DailyTrades> {

    public DailyTradesClientTab(TradeRulesClientTab tab) { super(tab); }

    @Override
    public TradeRuleType<DailyTrades> getType() { return DailyTrades.TYPE; }

    @Override
    protected void initialize(ScreenArea area) {

        //Reset Data Button
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(40,20))
                .ofWidth(area.width - 80)
                .onPress(this::clearData)
                .withText(DailyTrades.BUTTON_RESET)
                .tooltip(TooltipSource.simple(DailyTrades.TOOLTIP_RESET).withAutoWrap())
                .active(() -> this.getRule().dataSize() > 0)
                .build());

        //Delay Input
        this.addChild(TimeInputWidget.builder()
                .atPos(area.pos.offset(67,60))
                .startTime(this.getRule().getDelay())
                .withUnitRange(TimeUnit.MINUTE,TimeUnit.DAY)
                .withDurationRange(TimeHelper.DURATION_MINUTE,TimeHelper.DURATION_DAY * 30)
                .withHandler(this::setDelay)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.centeredTextWithWordWrap(DailyTrades.GUI_INFO.get(new TimeData(this.getRule().getDelay()).getString()),area.halfWidth(),100,area.width - 40,0xFF404040,false);
    }

    private void clearData() { this.requestChange(FancyPacketMap.flag("clearData")); }
    private void setDelay(TimeData newDelay) { this.requestChange(FancyPacketMap.map().setLong("setDelay",newDelay.milliseconds)); }

}
