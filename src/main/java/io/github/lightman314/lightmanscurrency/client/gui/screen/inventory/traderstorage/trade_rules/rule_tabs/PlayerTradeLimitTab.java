package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerTradeLimit;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class PlayerTradeLimitTab extends TradeRuleSubTab<PlayerTradeLimit> {


    public PlayerTradeLimitTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PlayerTradeLimit.TYPE); }

    EditBox limitInput;
    EasyButton buttonClearMemory;

    TimeInputWidget timeInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        PlayerTradeLimit rule = this.getRule();

        this.limitInput = this.addChild(TextInputUtil.intBuilder()
                .position(screenArea.pos.offset(10,19))
                .size(30,20)
                .maxLength(3)
                .parser(IntParser.ONE_TO_ONE_HUNDRED)
                .handler(this::onLimitChanged)
                .startingValue(rule == null ? 1 : rule.getLimit())
                .build());

        this.buttonClearMemory = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(10,55))
                .width(screenArea.width - 20)
                .text(LCText.BUTTON_CLEAR_MEMORY)
                .pressAction(this::PressClearMemoryButton)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADE_LIMIT_CLEAR_MEMORY))
                .build());

        this.timeInput = this.addChild(TimeInputWidget.builder()
                .position(screenArea.pos.offset(63,92))
                .unitRange(TimeUtil.TimeUnit.MINUTE, TimeUtil.TimeUnit.DAY)
                .handler(this::onTimeSet)
                .startTime(rule.getTimeLimit())
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        PlayerTradeLimit rule = this.getRule();
        if(rule != null)
        {
            gui.drawString(LCText.GUI_TRADE_LIMIT_INFO.get(rule.getLimit()), 10, 9, 0x404040);

            Component text = this.getRule().getTimeLimit() > 0 ? LCText.GUI_PLAYER_TRADE_LIMIT_DURATION.get(new TimeUtil.TimeData(rule.getTimeLimit()).getShortString()) : LCText.GUI_PLAYER_TRADE_LIMIT_NO_DURATION.get();
            TextRenderUtil.drawCenteredText(gui, text, this.screen.getXSize() / 2, 80, 0x404040);

        }

    }

    void onLimitChanged(int newLimit)
    {
        PlayerTradeLimit rule = this.getRule();
        if(rule != null)
            rule.setLimit(newLimit);
        this.sendUpdateMessage(this.builder().setInt("Limit", newLimit));
    }

    void PressClearMemoryButton(EasyButton button)
    {
        PlayerTradeLimit rule = this.getRule();
        if(rule != null)
            rule.resetMemory();
        this.sendUpdateMessage(this.builder().setFlag("ClearMemory"));
    }

    public void onTimeSet(TimeUtil.TimeData newTime) {
        long timeLimit = MathUtil.clamp(newTime.miliseconds, 0, Long.MAX_VALUE);
        PlayerTradeLimit rule = this.getRule();
        if(rule != null)
            rule.setTimeLimit(timeLimit);
        this.sendUpdateMessage(this.builder().setLong("TimeLimit", timeLimit));
    }

}