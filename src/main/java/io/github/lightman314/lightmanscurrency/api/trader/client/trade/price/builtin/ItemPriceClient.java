package io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.price.PriceInputHandler;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.price.builtin.ItemPriceInputHandler;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.TradeButtonDisplay;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.ItemPrice;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.ClientTradePrice;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeButtonDisplay;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
                if(!item.isEmpty() || context.isEditingView())
                    entries.add(ItemTradeButtonDisplay.forTradeItem(item,TradeSlotType.INPUT,TradeButtonDisplay.EMPTY_ITEM,context,handler));
            }
            return entries;
        }
        return List.of();
    }

    @Override
    public void collectPriceInputs(Player player, Consumer<PriceInputHandler> builder) {
        builder.accept(new ItemPriceInputHandler());
    }

}