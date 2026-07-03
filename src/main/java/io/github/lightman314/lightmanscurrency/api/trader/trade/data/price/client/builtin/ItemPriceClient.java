package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.client.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.TradeDisplay;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.ItemPrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.client.ClientTradePrice;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeDisplay;

import java.util.ArrayList;
import java.util.List;

public class ItemPriceClient implements ClientTradePrice {

    public static final ClientTradePrice INSTANCE = new ItemPriceClient();

    private ItemPriceClient() {}

    @Override
    public List<TradeDisplayEntry> priceDisplay(TradePrice price,TradeContext context,int width,ITradeInteractionHandler handler) {
        if(price instanceof ItemPrice ip)
        {
            List<TradeDisplayEntry> entries = new ArrayList<>();
            for(int i = 0; i < 2; ++i)
            {
                TradeItem item = ip.getItem(i);
                if(!item.isEmpty() || context.getCustomer().isEditing())
                    entries.add(ItemTradeDisplay.forTradeItem(item,TradeDisplay.EMPTY_ITEM,context,handler));
            }
            return entries;
        }
        return List.of();
    }

    @Override
    public void onPriceSelection(IWidgetHolder screen,ScreenArea area,TradePrice price, TradeContext context, int slot, ITradeInteractionHandler handler) {
        //TODO item edit widget, with a
    }

}