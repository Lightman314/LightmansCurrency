package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.client;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.client.ClientPairedRegistry;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;

import java.util.List;

public interface ClientTradePrice {

    ClientPairedRegistry<TradePriceType<?>, ClientTradePrice> REGISTRY = new ClientPairedRegistry<>(LCRegistries.Trader.TRADE_PRICE_TYPE);

    int EXPECTED_WIDTH = 33;

    static List<TradeDisplayEntry> getTradeDisplay(TradeData trade, TradeContext context, int width, ITradeInteractionHandler handler) {
        TradePrice price = trade.getPrice(context);
        ClientTradePrice display = REGISTRY.getValue(price.getType());
        return display.priceDisplay(price,context,width,handler);
    }

    List<TradeDisplayEntry> priceDisplay(TradePrice price,TradeContext context,int width,ITradeInteractionHandler handler);

    void onPriceSelection(IWidgetHolder screen,ScreenArea area,TradePrice price, TradeContext context, int slot, ITradeInteractionHandler handler);

}
