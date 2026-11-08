package io.github.lightman314.lightmanscurrency.client.features.tickets;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.price.PriceInputHandler;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays.ItemDisplay;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketID;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.ClientTradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.features.tickets.trader.TicketPrice;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Consumer;

public class TicketPriceClient implements ClientTradePrice {

    public static final ClientTradePrice INSTANCE = new TicketPriceClient();

    public static final Identifier EMPTY_SLOT_BACKGROUND = LCApi.id("container/slot/ticket");

    private TicketPriceClient() {}

    @Override
    public void collectPriceInputs(Player player,Consumer<PriceInputHandler> builder) {
        builder.accept(new TicketPriceInputHandler());
    }

    @Override
    public List<TradeDisplayEntry> priceDisplay(TradePrice price,TradeContext context,int width,ITradeInteractionHandler handler) {
        if(price instanceof TicketPrice tp)
            return List.of(ItemDisplay.of(tp.getDisplayStack(),EMPTY_SLOT_BACKGROUND,(t, i) -> {
                t.clear();
                if(tp.isValid()) {
                    //Ticket ID Tooltip
                    t.add(TicketID.TOOLTIP.get(tp.getTicketID()));
                    t.add(TicketPrice.TOOLTIP_USES.get(tp.getCount()));
                }
            }));
        return List.of();
    }

}