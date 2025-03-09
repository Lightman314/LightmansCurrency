package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.menus.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.TraderMoneyStorageTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TraderMoneyStorageClientTab extends TraderStorageClientTab<TraderMoneyStorageTab> {

    private MoneyValueWidget amountWidget = null;

    public TraderMoneyStorageClientTab(Object screen, TraderMoneyStorageTab commonTab) { super(screen, commonTab); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        //Amount Widget
        this.amountWidget = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.centerX() - MoneyValueWidget.WIDTH / 2, screenArea.y + 10)
                .oldIfNotFirst(firstOpen,this.amountWidget)
                .blockFreeInputs()
                .build());

        //Store Button
        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,85))
                .width(80)
                .text(LCText.BUTTON_TRADER_STORE_MONEY)
                .pressAction(this::storeMoney)
                .addon(EasyAddonHelper.activeCheck(this::storeButtonActive))
                .build());

        //Collect Button
        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 100,85))
                .width(80)
                .text(LCText.BUTTON_TRADER_COLLECT_MONEY)
                .pressAction(this::collectMoney)
                .addon(EasyAddonHelper.activeCheck(this::collectButtonActive))
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            //Render current balance
            TextRenderUtil.drawCenteredText(gui,LCText.GUI_TRADER_MONEY_STORAGE_CONTENTS.get(trader.getInternalStoredMoney().getRandomValueText()), TraderScreen.WIDTH / 2, 110, 0x404040);
        }

        //Render Coin Slots
        for(MoneySlot slot : this.commonTab.getCoinSlots())
            gui.renderSlot(this.screen,slot);
    }

    private boolean storeButtonActive()
    {
        if(!this.commonTab.canStoreMoney())
            return false;
        IMoneyHandler coinSlotHandler = this.commonTab.getCoinSlotHandler();
        //If no money in the coin slots, only allow storage if an amount is defined
        return !coinSlotHandler.getStoredMoney().isEmpty() || !this.amountWidget.getCurrentValue().isEmpty();
    }

    private boolean collectButtonActive()
    {
        if(!this.commonTab.canCollectMoney())
            return false;
        TraderData trader = this.menu.getTrader();
        return trader != null && !trader.getInternalStoredMoney().isEmpty();
    }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_STORE_COINS; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_MONEY_STORAGE.get(); }

    private void storeMoney() {
        if(this.amountWidget == null)
            return;
        this.commonTab.storeMoney(this.amountWidget.getCurrentValue());
        this.amountWidget.changeValue(MoneyValue.empty());
    }

    private void collectMoney() {
        if(this.amountWidget == null)
            return;
        this.commonTab.collectMoney(this.amountWidget.getCurrentValue());
        this.amountWidget.changeValue(MoneyValue.empty());
    }

}