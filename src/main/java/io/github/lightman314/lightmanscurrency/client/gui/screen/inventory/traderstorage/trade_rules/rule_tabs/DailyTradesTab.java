package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.DailyTrades;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;

import javax.annotation.Nonnull;

public class DailyTradesTab extends TradeRuleSubTab<DailyTrades> {

    public DailyTradesTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent,DailyTrades.TYPE); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        DailyTrades rule = this.getRule();

        //Reset Data Button
        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(40,20))
                .width(screenArea.width - 80)
                .pressAction(this::clearData)
                .text(LCText.BUTTON_DAILY_TRADES_RESET)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_DAILY_TRADES_RESET, TooltipHelper.DEFAULT_TOOLTIP_WIDTH))
                .addon(EasyAddonHelper.activeCheck(() -> {
                    DailyTrades r = this.getRule();
                    return r != null && r.dataSize() > 0;
                }))
                .build());

        //Delay Input
        this.addChild(TimeInputWidget.builder()
                .position(screenArea.pos.offset(63,60))
                .startTime(rule == null ? TimeUtil.DURATION_DAY : rule.getInteractionDelay())
                .unitRange(TimeUtil.TimeUnit.MINUTE,TimeUtil.TimeUnit.DAY)
                .minDuration(TimeUtil.DURATION_MINUTE)
                .maxDuration(TimeUtil.DURATION_DAY * 30)
                .handler(this::setDelay)
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        DailyTrades rule = this.getRule();
        if(rule == null)
            return;

        TextRenderUtil.drawCenteredMultilineText(gui,LCText.GUI_DAILY_TRADES_INFO.get(new TimeUtil.TimeData(rule.getInteractionDelay()).getString()), 20, this.screen.getXSize() - 40, 100, 0x404040);

    }

    private void clearData() { this.sendUpdateMessage(this.builder().setFlag("ClearData")); }

    private void setDelay(TimeUtil.TimeData newDelay) {
        this.sendUpdateMessage(this.builder().setLong("SetDelay",newDelay.miliseconds));
    }

}