package io.github.lightman314.lightmanscurrency.common.money.ancient_money.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyValue;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class AncientCoinValueInput extends MoneyInputHandler {

    AncientCoinType selectedType = AncientCoinType.COPPER;
    long count = 0;

    public AncientCoinValueInput() { }

    @Nonnull
    @Override
    public MutableComponent inputName() { return LCText.ANCIENT_COIN_VALUE_NAME.get(); }

    //Generate matching unique name for the money value with this ancient money type
    @Nonnull
    @Override
    public String getUniqueName() { return MoneyValue.generateCustomUniqueName(AncientMoneyType.TYPE,selectedType.resourceSafeName()); }

    @Override
    public boolean isForValue(@Nonnull MoneyValue value) { return value instanceof AncientMoneyValue; }

    @Override
    public void initialize(@Nonnull ScreenArea widgetArea) {

        //Recalibrate the selected coin type
        this.onValueChanged(this.currentValue());

        int buttonX = (widgetArea.width / 2) - 10;

        //Previous Type Button
        this.addChild(PlainButton.builder()
                .position(widgetArea.pos.offset(buttonX - 30, 33))
                .pressAction(this::PreviousType)
                .sprite(MoneyValueWidget.SPRITE_LEFT_ARROW)
                .addon(EasyAddonHelper.visibleCheck(() -> this.isVisible() && this.canChangeHandler()))
                .build());

        //Next Type Button
        this.addChild(PlainButton.builder()
                .position(widgetArea.pos.offset(buttonX + 40, 33))
                .pressAction(this::NextType)
                .sprite(MoneyValueWidget.SPRITE_RIGHT_ARROW)
                .addon(EasyAddonHelper.visibleCheck(() -> this.isVisible() && this.canChangeHandler()))
                .build());

        //Increase Button
        this.addChild(PlainButton.builder()
                .position(widgetArea.pos.offset(buttonX,19))
                .pressAction(this::IncreaseCount)
                .sprite(MoneyValueWidget.SPRITE_UP_ARROW)
                .addon(EasyAddonHelper.activeCheck(() -> !this.isFree()))
                .addon(EasyAddonHelper.visibleCheck(this::isVisible))
                .build());

        //Decrease Button
        this.addChild(PlainButton.builder()
                .position(widgetArea.pos.offset(buttonX, 57))
                .pressAction(this::DecreaseCount)
                .sprite(MoneyValueWidget.SPRITE_DOWN_ARROW)
                .addon(EasyAddonHelper.activeCheck(() -> this.count > 0 && !this.isFree()))
                .addon(EasyAddonHelper.visibleCheck(this::isVisible))
                .build());

    }

    @Override
    protected void renderBG(@Nonnull ScreenArea widgetArea, @Nonnull EasyGuiGraphics gui) {

        //Draw Coin Sprite
        gui.renderItem(this.selectedType.asItem(), (widgetArea.width / 2) - 8, 30);
        //Draw String
        TextRenderUtil.drawCenteredText(gui, String.valueOf(this.count), widgetArea.width / 2, 47, 0x404040);

    }

    @Override
    public void onValueChanged(@Nonnull MoneyValue newValue) {
        if(newValue instanceof AncientMoneyValue value)
        {
            this.selectedType = value.type;
            this.count = value.count;
        }
        else
            this.count = 0;
    }

    private void changeValue() { this.changeValue(this.buildValue()); }
    private MoneyValue buildValue()
    {
        if(this.count > 0)
            return AncientMoneyValue.of(this.selectedType,this.count);
        return this.isFree() ? MoneyValue.free() : MoneyValue.empty();
    }

    private void NextType()
    {
        this.selectedType = this.selectedType.next();
        this.onInternalHandlerChange();
        this.changeValue();
    }

    private void PreviousType()
    {
        this.selectedType = this.selectedType.previous();
        this.onInternalHandlerChange();
        this.changeValue();
    }

    private void IncreaseCount()
    {
        int addAmount = 1;
        if(Screen.hasShiftDown())
            addAmount = 10;
        if(Screen.hasControlDown())
            addAmount *= 10;
        this.count += addAmount;
        this.changeValue();
    }

    private void DecreaseCount()
    {
        int removeAmount = 1;
        if(Screen.hasShiftDown())
            removeAmount = 10;
        if(Screen.hasControlDown())
            removeAmount *= 10;
        this.count = Math.max(0,this.count - removeAmount);
        this.changeValue();
    }

}