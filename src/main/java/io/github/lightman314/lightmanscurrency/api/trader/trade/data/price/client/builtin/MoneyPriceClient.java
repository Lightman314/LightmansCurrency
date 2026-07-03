package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.client.builtin;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.client.ClientPairedRegistry;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.builtin.OverlappingItemDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.builtin.TextDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueType;
import io.github.lightman314.lightmanscurrency.api.money.values.interfaces.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.client.ClientTradePrice;

import java.util.List;

public abstract class MoneyPriceClient {

    public static final ClientPairedRegistry<MoneyValueType<?>, MoneyPriceClient> REGISTRY = new ClientPairedRegistry<>(LCRegistries.Money.VALUE_TYPE, MoneyPriceClient::simpleText);

    public static final ClientTradePrice INSTANCE = new Impl();

    public static MoneyPriceClient simpleText() { return TextMoneyClient.INSTANCE; }
    public static MoneyPriceClient itemValueDisplay() { return ItemValueClient.INSTANCE; }

    protected List<TradeDisplayEntry> priceDisplay(TradePrice price,TradeContext context,int width,ITradeInteractionHandler handler) {
        if(price instanceof MoneyPrice money)
            return this.moneyDisplay(money.getPrice(),context,width,handler);
        return List.of();
    }

    protected abstract List<TradeDisplayEntry> moneyDisplay(MoneyValue value,TradeContext context,int width,ITradeInteractionHandler handler);

    private static class Impl implements ClientTradePrice
    {
        @Override
        public List<TradeDisplayEntry> priceDisplay(TradePrice price,TradeContext context,int width,ITradeInteractionHandler handler) {
            if(price instanceof MoneyPrice money)
            {
                MoneyPriceClient display = MoneyPriceClient.REGISTRY.getValue(money.getPrice().getType());
                return display.priceDisplay(price,context,width,handler);
            }
            return List.of();
        }

        @Override
        public void onPriceSelection(IWidgetHolder screen,ScreenArea area,TradePrice price,TradeContext context,int slot,ITradeInteractionHandler handler) {
            if(price instanceof MoneyPrice money)
            {
                MoneyValue value = money.getPrice();
                //TODO add money value widget
            }
        }
    }

    private static class TextMoneyClient extends MoneyPriceClient {

        public static final MoneyPriceClient INSTANCE = new TextMoneyClient();

        @Override
        protected List<TradeDisplayEntry> moneyDisplay(MoneyValue value,TradeContext context,int width,ITradeInteractionHandler handler) {
            return List.of(TextDisplay.of(value.getText(),width));
        }
    }

    private static class ItemValueClient extends MoneyPriceClient {

        public static final MoneyPriceClient INSTANCE = new ItemValueClient();

        @Override
        protected List<TradeDisplayEntry> moneyDisplay(MoneyValue value,TradeContext context,int width,ITradeInteractionHandler handler) {
            if(value instanceof IItemBasedValue v)
                return List.of(OverlappingItemDisplay.of(v.getAsItemList(),List.of(value.getText()),width));
            return List.of();
        }

    }

}