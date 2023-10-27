package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerTradeLimit;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class PlayerTradeLimitTab extends TradeRuleSubTab<PlayerTradeLimit> {


    public PlayerTradeLimitTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PlayerTradeLimit.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_COUNT_PLAYER; }

    EditBox limitInput;
    EasyButton buttonSetLimit;
    EasyButton buttonClearMemory;

    TimeInputWidget timeInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.limitInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 19, 30, 20, EasyText.empty()));
        this.limitInput.setMaxLength(3);
        PlayerTradeLimit rule = this.getRule();
        if(rule != null)
            this.limitInput.setValue(Integer.toString(rule.getLimit()));

        this.buttonSetLimit = this.addChild(new EasyTextButton(screenArea.pos.offset(41, 19), 40, 20, EasyText.translatable("gui.button.lightmanscurrency.playerlimit.setlimit"), this::PressSetLimitButton));
        this.buttonClearMemory = this.addChild(new EasyTextButton(screenArea.pos.offset(10, 50), screenArea.width - 20, 20, EasyText.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory"), this::PressClearMemoryButton)
                .withAddons(EasyAddonHelper.tooltip(EasyText.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory.tooltip"))));

        this.timeInput = this.addChild(new TimeInputWidget(screenArea.pos.offset(63, 87), 10, TimeUtil.TimeUnit.DAY, TimeUtil.TimeUnit.MINUTE, this::onTimeSet));
        this.timeInput.setTime(this.getRule().getTimeLimit());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        PlayerTradeLimit rule = this.getRule();
        if(rule != null)
            gui.drawString(EasyText.translatable("gui.button.lightmanscurrency.playerlimit.info", rule.getLimit()).getString(), 10, 9, 0xFFFFFF);

        Component text = this.getRule().getTimeLimit() > 0 ? EasyText.translatable("gui.widget.lightmanscurrency.playerlimit.duration", new TimeUtil.TimeData(this.getRule().getTimeLimit()).getShortString()) : EasyText.translatable("gui.widget.lightmanscurrency.playerlimit.noduration");
        TextRenderUtil.drawCenteredText(gui, text, this.screen.getXSize() / 2, 75, 0xFFFFFF);

    }

    @Override
    public void tick() { TextInputUtil.whitelistInteger(this.limitInput, 1, 100); }

    void PressSetLimitButton(EasyButton button)
    {
        int limit = MathUtil.clamp(TextInputUtil.getIntegerValue(this.limitInput), 1, 100);
        PlayerTradeLimit rule = this.getRule();
        if(rule != null)
            rule.setLimit(limit);
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putInt("Limit", limit);
        this.sendUpdateMessage(updateInfo);
    }

    void PressClearMemoryButton(EasyButton button)
    {
        PlayerTradeLimit rule = this.getRule();
        if(rule != null)
            rule.resetMemory();
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putBoolean("ClearMemory", true);
        this.sendUpdateMessage(updateInfo);
    }

    public void onTimeSet(TimeUtil.TimeData newTime) {
        long timeLimit = MathUtil.clamp(newTime.miliseconds, 0, Long.MAX_VALUE);
        PlayerTradeLimit rule = this.getRule();
        if(rule != null)
            rule.setTimeLimit(timeLimit);
        CompoundTag updateInfo = new CompoundTag();
        updateInfo.putLong("TimeLimit", timeLimit);
        this.sendUpdateMessage(updateInfo);
    }

}
