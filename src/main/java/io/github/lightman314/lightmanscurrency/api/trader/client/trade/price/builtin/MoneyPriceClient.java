package io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyPriceInputWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.price.PriceInputHandler;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyValueType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.ClientTradePrice;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Consumer;

public final class MoneyPriceClient implements ClientTradePrice {

    public static final ClientTradePrice INSTANCE = new MoneyPriceClient();

    private MoneyPriceClient() {}

    @Override
    public List<TradeDisplayEntry> priceDisplay(TradePrice price,TradeContext context,int width,ITradeInteractionHandler handler) {
        if(price instanceof MoneyPrice money)
        {
            ClientMoneyValueType display = ClientMoneyValueType.REGISTRY.getValue(money.getPrice().getType());
            return display.priceDisplay(price,context,width,handler);
        }
        return List.of();
    }
    
    @Override
    public void collectPriceInputs(Player player, Consumer<PriceInputHandler> builder) {
        //Wrap MoneyInputHandlers to work as PriceInputHandlers as well
        Consumer<MoneyInputHandler> handlerBuilder = h -> builder.accept(new MoneyPriceInputWrapper(h));
        for(ClientMoneyValueType type : ClientMoneyValueType.REGISTRY)
            type.collectMoneyInputs(player,handlerBuilder);
    }

}