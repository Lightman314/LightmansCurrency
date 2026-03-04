package io.github.lightman314.lightmanscurrency.api.traders.rules.client.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.rules.client.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.rules.client.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.client.widgets.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.api.client.widgets.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.widgets.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.traders.rules.builtin.TradeLimit;

import javax.annotation.Nonnull;

public class TradeLimitTab extends TradeRuleSubTab<TradeLimit> {

    public TradeLimitTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, TradeLimit.TYPE); }

    TextBoxWrapper<Integer> limitInput;
    EasyButton buttonClearMemory;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        TradeLimit rule = this.getRule();

        this.limitInput = this.addChild(TextInputUtil.intBuilder()
                .position(screenArea.pos.offset(10,19))
                .size(60,20)
                .apply(IntParser.builder().min(1).max(TradeLimit.MAX_LIMIT).consumer())
                .startingValue(rule == null ? 1 : rule.getLimit())
                .handler(this::onLimitChanged)
                .wrap().build());

        this.buttonClearMemory = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(10,55))
                .width(screenArea.width - 20)
                .text(LCText.BUTTON_CLEAR_MEMORY)
                .pressAction(this::PressClearMemoryButton)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADE_LIMIT_CLEAR_MEMORY))
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TradeLimit rule = this.getRule();
        if(rule != null)
            gui.drawString(LCText.GUI_TRADE_LIMIT_INFO.get(rule.getLimit()).getString(), 10, 9, 0x404040);


    }

    void onLimitChanged(int newLimit)
    {
        TradeLimit rule = this.getRule();
        if(rule != null)
            rule.setLimit(newLimit);
        this.sendUpdateMessage(this.builder().setInt("Limit", newLimit));
    }

    void PressClearMemoryButton(EasyButton button)
    {
        TradeLimit rule = this.getRule();
        if(rule != null)
            rule.resetCount();
        this.sendUpdateMessage(this.builder().setFlag("ClearMemory"));
    }

}
