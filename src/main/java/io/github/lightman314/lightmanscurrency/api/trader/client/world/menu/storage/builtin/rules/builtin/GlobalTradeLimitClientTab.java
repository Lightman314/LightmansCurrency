package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.IntParser;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.RuleClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.builtin.GlobalTradeLimit;

public class GlobalTradeLimitClientTab extends RuleClientTab<GlobalTradeLimit> {

    public GlobalTradeLimitClientTab(TradeRulesClientTab tab) { super(tab); }

    @Override
    public TradeRuleType<GlobalTradeLimit> getType() { return GlobalTradeLimit.TYPE; }

    private TextBoxWrapper<Integer> limitInput;

    @Override
    protected void initialize(ScreenArea area) {

        this.limitInput = this.addChild(TextBoxWrapper.intBuilder()
                .atPos(area.pos.offset(10,19))
                .ofWidth(60)
                .parser(IntParser.builder().min(1).max(GlobalTradeLimit.MAX_LIMIT).build())
                .withStartingValue(this.getRule().getLimit())
                .withOldWidget(this.limitInput)
                .withHandler(this::onLimitChanged)
                .build());

        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(10,55))
                .ofWidth(area.width - 20)
                .withText(GlobalTradeLimit.BUTTON_CLEAR_MEMORY)
                .onPress(this::clearMemory)
                .tooltip(TooltipSource.simple(GlobalTradeLimit.TOOLTIP_CLEAR_MEMORY))
                .active(this::canClearMemory)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

        gui.centeredTextWithWordWrap(GlobalTradeLimit.GUI_INFO.get(this.getRule().getLimit()),area.halfWidth(),9,area.width - 10,0xFF404040,false);

    }

    private void onLimitChanged(int newLimit) {
        this.requestChange(FancyPacketMap.map().setInt("setLimit",newLimit));
    }

    private void clearMemory() {
        this.requestChange(FancyPacketMap.flag("clearCount"));
    }

    private boolean canClearMemory() {
        return this.getRule().getCount() > 0;
    }

}