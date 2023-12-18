package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
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

        this.buttonSetLimit = this.addChild(new EasyTextButton(screenArea.pos.offset(41, 19), 40, 20, EasyText.translatable("gui.button.lightmanscurrency.playerlimit.setlimit"), this::PressSetLimitButton));
        this.buttonClearMemory = this.addChild(new EasyTextButton(screenArea.pos.offset(10, 50), screenArea.width - 20, 20, EasyText.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory"), this::PressClearMemoryButton)
                .withAddons(EasyAddonHelper.tooltip(EasyText.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory.tooltip"))));

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TradeLimit rule = this.getRule();
        if(rule != null)
            gui.drawString(EasyText.translatable("gui.button.lightmanscurrency.playerlimit.info", rule.getLimit()).getString(), 10, 9, 0xFFFFFF);


    }

    @Override
    public void tick() { TextInputUtil.whitelistInteger(this.limitInput, 1, 100); }

    void PressSetLimitButton(EasyButton button)
    {
        int limit = MathUtil.clamp(TextInputUtil.getIntegerValue(this.limitInput), 1, 100);
        TradeLimit rule = this.getRule();
        if(rule != null)
            rule.setLimit(limit);
        this.sendUpdateMessage(LazyPacketData.simpleInt("Limit", limit));
    }

    void PressClearMemoryButton(EasyButton button)
    {
        TradeLimit rule = this.getRule();
        if(rule != null)
            rule.resetCount();
        this.sendUpdateMessage(LazyPacketData.simpleFlag("ClearMemory"));
    }

}
