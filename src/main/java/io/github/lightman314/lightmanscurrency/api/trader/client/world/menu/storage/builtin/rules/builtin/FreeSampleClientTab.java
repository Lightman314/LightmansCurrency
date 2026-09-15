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
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeUnit;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.RuleClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.FreeSample;
import net.minecraft.network.chat.Component;

public class FreeSampleClientTab extends RuleClientTab<FreeSample> {

    public FreeSampleClientTab(TradeRulesClientTab tab) { super(tab); }

    @Override
    public TradeRuleType<FreeSample> getType() { return FreeSample.TYPE; }

    @Override
    protected void initialize(ScreenArea area) {
        FreeSample rule = this.getRule();

        //Limit Input
        this.addChild(TextBoxWrapper.intBuilder()
                .atPos(area.pos.offset(10,19))
                .ofWidth(30)
                .parser(IntParser.builder()
                        .min(1)
                        .max(FreeSample.MAX_LIMIT).build())
                .withHandler(this::onLimitChanged)
                .withMaxLength(3)
                .withStartingString(String.valueOf(rule.getLimit()))
                .build());

        //Reset Memory Button
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(10,55))
                .ofWidth(area.width - 20)
                .withText(FreeSample.BUTTON_CLEAR_MEMORY)
                .onPress(this::clearMemory)
                .tooltip(TooltipSource.simple(FreeSample.TOOLTIP_CLEAR_MEMORY).withAutoWrap())
                .build());

        //Time Input
        this.addChild(TimeInputWidget.builder()
                .atPos(area.pos.offset(65,102))
                .withUnitRange(TimeUnit.MINUTE,TimeUnit.DAY)
                .withHandler(this::onTimeSet)
                .startTime(rule.getTimer())
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

        FreeSample rule = this.getRule();

        gui.text(FreeSample.GUI_INFO.get(rule.getLimit()),10,9,0xFF404040,false);
        gui.centeredTextWithWordWrap(FreeSample.GUI_PLAYER_COUNT.get(rule.getSampleCount()),area.halfWidth(),44,area.width - 20,0xFF404040,false);

        Component text = rule.getTimer() > 0 ? FreeSample.GUI_DURATION.get(new TimeData(rule.getTimer()).getString()) : FreeSample.GUI_NO_DURATION.get();
        gui.centeredTextWithWordWrap(text,area.halfWidth(),80,area.width - 20,0xFF404040,false);

    }

    private void onLimitChanged(int newLimit) {
        this.requestChange(FancyPacketMap.map()
                .setInt("setLimit",newLimit));
    }

    private void clearMemory() {
        this.requestChange(FancyPacketMap.flag("clearMemory"));
    }

    protected void onTimeSet(TimeData newTime) {
        this.requestChange(FancyPacketMap.map()
                .setLong("setTimer",newTime.milliseconds));
    }


}
