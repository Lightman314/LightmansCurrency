package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.TradeLimit;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.EditBox;

import javax.annotation.Nonnull;

public class TradeLimitTab extends TradeRuleSubTab<TradeLimit> {

    public TradeLimitTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, TradeLimit.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_COUNT; }

    EditBox limitInput;
    EasyButton buttonSetLimit;
    EasyButton buttonClearMemory;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.limitInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 19, 30, 20, EasyText.empty()));
        this.limitInput.setMaxLength(3);
        TradeLimit rule = this.getRule();
        if(rule != null)
            this.limitInput.setValue(Integer.toString(rule.getLimit()));

        this.buttonSetLimit = this.addChild(new EasyTextButton(screenArea.pos.offset(41, 19), 40, 20, LCText.BUTTON_SET.get(), this::PressSetLimitButton));
        this.buttonClearMemory = this.addChild(new EasyTextButton(screenArea.pos.offset(10, 50), screenArea.width - 20, 20, LCText.BUTTON_CLEAR_MEMORY.get(), this::PressClearMemoryButton)
                .withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADE_LIMIT_CLEAR_MEMORY)));

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TradeLimit rule = this.getRule();
        if(rule != null)
            gui.drawString(LCText.GUI_TRADE_LIMIT_INFO.get(rule.getLimit()).getString(), 10, 9, 0xFFFFFF);


    }

    @Override
    public void tick() { TextInputUtil.whitelistInteger(this.limitInput, 1, 100); }

    void PressSetLimitButton(EasyButton button)
    {
        int limit = MathUtil.clamp(TextInputUtil.getIntegerValue(this.limitInput), 1, 100);
        TradeLimit rule = this.getRule();
        if(rule != null)
            rule.setLimit(limit);
        this.sendUpdateMessage(this.builder().setInt("Limit", limit));
    }

    void PressClearMemoryButton(EasyButton button)
    {
        TradeLimit rule = this.getRule();
        if(rule != null)
            rule.resetCount();
        this.sendUpdateMessage(this.builder().setFlag("ClearMemory"));
    }

}
