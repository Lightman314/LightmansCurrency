package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.MultiPriceTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MultiPriceClientTab extends TraderStorageClientTab<MultiPriceTab> {

    MoneyValueWidget priceInput;

    public MultiPriceClientTab(Object screen, MultiPriceTab commonTab) { super(screen, commonTab); }

    @Override
    public boolean tabVisible() { return false; }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.Null(); }

    @Nullable
    @Override
    public Component getTooltip() { return EasyText.empty(); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.priceInput = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.centerX() - MoneyValueWidget.WIDTH / 2,screenArea.y + 10)
                .oldIfNotFirst(firstOpen,this.priceInput)
                .startingValue(this.getCommonPrice())
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(30,100))
                .text(this::getSetPriceText)
                .pressAction(this::setTradePrices)
                .width(screenArea.width - 60)
                .addon(EasyAddonHelper.activeCheck(this::canSetPrice))
                .build());

    }

    private Component getSetPriceText() { return LCText.BUTTON_TRADER_SET_ALL_PRICES.get(this.commonTab.selectedCount()); }

    private boolean hasCommonPrice()
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            MoneyValue price;
            List<TradeData> trades = this.commonTab.getSelectedTrades();
            if(trades.isEmpty())
                return false;
            price = trades.get(0).getCost();
            if(trades.size() == 1)
                return true;
            else
            {
                for(int i = 1; i < trades.size(); ++i)
                {
                    //If not all trades have the same price, don't use a default value
                    if(!price.equals(trades.get(i).getCost()))
                        return false;
                }
            }
            return true;
        }
        return false;
    }

    @Nonnull
    private MoneyValue getCommonPrice()
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            MoneyValue price;
            List<TradeData> trades = this.commonTab.getSelectedTrades();
            if(trades.isEmpty())
                return MoneyValue.empty();
            price = trades.get(0).getCost();
            if(trades.size() == 1)
                return price;
            else
            {
                for(int i = 1; i < trades.size(); ++i)
                {
                    //If not all trades have the same price, don't use a default value
                    if(!price.equals(trades.get(i).getCost()))
                        return MoneyValue.empty();
                }
            }
            return price;
        }
        return MoneyValue.empty();
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    private boolean canSetPrice()
    {
        if(this.priceInput == null)
            return false;
        MoneyValue price = this.priceInput.getCurrentValue();
        return !this.hasCommonPrice() || !price.equals(this.getCommonPrice());
    }

    private void setTradePrices()
    {
        this.commonTab.setPrices(this.priceInput.getCurrentValue());
    }

}