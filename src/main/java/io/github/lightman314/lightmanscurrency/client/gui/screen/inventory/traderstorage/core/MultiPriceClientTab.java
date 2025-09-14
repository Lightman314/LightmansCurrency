package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.MultiPriceTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiPriceClientTab extends TraderStorageClientTab<MultiPriceTab> {

    DropdownWidget directionInput;
    MoneyValueWidget priceInput;
    private final List<TradeDirection> directionOptions = new ArrayList<>();

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

        int startingIndex = this.setupDirectionOptions();
        if(this.directionOptions.size() > 1)
        {
            //Dropdown for selection options
            this.directionInput = this.addChild(DropdownWidget.builder()
                    .position(screenArea.pos.offset(screenArea.width - 100,19))
                    .width(80)
                    .options(this.directionOptions.stream().map(d -> (Component)d.getName()).toList())
                    .selected(startingIndex)
                    .build());

            this.addChild(EasyTextButton.builder()
                    .position(screenArea.pos.offset(20,15))
                    .text(this::getSetDirectionText)
                    .width(screenArea.width - 130)
                    .pressAction(this::setTradeDirections)
                    .addon(EasyAddonHelper.activeCheck(this::canSetDirection))
                    .build());

        }
        else
            this.directionInput = null;

        this.priceInput = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.centerX() - MoneyValueWidget.WIDTH / 2,screenArea.y + 40)
                .oldIfNotFirst(firstOpen,this.priceInput)
                .startingValue(this.getCommonPrice())
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(30,120))
                .text(this::getSetPriceText)
                .pressAction(this::setTradePrices)
                .width(screenArea.width - 60)
                .addon(EasyAddonHelper.activeCheck(this::canSetPrice))
                .build());

    }

    private Component getSetPriceText() { return LCText.BUTTON_TRADER_SET_ALL_PRICES.get(this.commonTab.selectedCount()); }

    private Component getSetDirectionText() { return LCText.BUTTON_TRADER_SET_ALL_DIRECTIONS.get(this.commonTab.selectedCount()); }

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

    private int setupDirectionOptions()
    {
        TraderData trader = this.menu.getTrader();
        this.directionOptions.clear();
        if(trader != null)
        {
            this.directionOptions.addAll(trader.validDirectionOptions());
            if(this.directionOptions.size() > 1)
            {
                List<TradeData> trades = this.commonTab.getSelectedTrades();
                if(trades.isEmpty())
                    return 0;
                TradeDirection result = trades.get(0).getTradeDirection();
                if(trades.size() > 1)
                {
                    for(int i = 1; i < trades.size(); ++i)
                    {
                        if(trades.get(i).getTradeDirection() != result)
                            return 0;
                    }
                }
                return Math.max(0,this.directionOptions.indexOf(result));
            }
        }
        return 0;
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

    private boolean canSetDirection()
    {
        if(this.directionOptions.size() <= 1 || this.directionInput == null)
            return false;
        TradeDirection direction = this.directionOptions.get(this.directionInput.getCurrentlySelected());
        for(TradeData trade : this.commonTab.getSelectedTrades())
        {
            if(trade.getTradeDirection() != direction)
                return true;
        }
        return false;
    }

    private void setTradeDirections()
    {
        if(this.directionInput == null)
            return;
        this.commonTab.setTradeDirection(this.directionOptions.get(this.directionInput.getCurrentlySelected()));
    }

}