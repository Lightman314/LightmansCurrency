package io.github.lightman314.lightmanscurrency.api.money.client;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.client.ClientPairedRegistry;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays.OverlappingItemDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays.TextDisplay;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueType;
import io.github.lightman314.lightmanscurrency.api.money.values.interfaces.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Consumer;

public abstract class ClientMoneyValueType {

    private static final ClientMoneyValueType DEFAULT = new Default();

    public static final ClientPairedRegistry<MoneyValueType<?>,ClientMoneyValueType> REGISTRY = ClientPairedRegistry.builder(LCRegistries.Money.VALUE_TYPE,ClientMoneyValueType.class)
            .defaultValue(DEFAULT).build();

    public List<TradeDisplayEntry> priceDisplay(TradePrice price, TradeContext context, int width, ITradeInteractionHandler handler) {
        if(price instanceof MoneyPrice money)
            return this.moneyDisplay(money.getPrice(),context,width,handler);
        return List.of();
    }

    protected abstract List<TradeDisplayEntry> moneyDisplay(MoneyValue value, TradeContext context, int width, ITradeInteractionHandler handler);

    public abstract void collectMoneyInputs(Player player,Consumer<MoneyInputHandler> builder);

    public abstract static class WithTextDisplay extends ClientMoneyValueType {
        @Override
        protected List<TradeDisplayEntry> moneyDisplay(MoneyValue value, TradeContext context, int width, ITradeInteractionHandler handler) {
            return List.of(TextDisplay.of(value.getText(),width));
        }
    }

    public abstract static class WithItemDisplay extends ClientMoneyValueType {
        @Override
        protected List<TradeDisplayEntry> moneyDisplay(MoneyValue value,TradeContext context,int width,ITradeInteractionHandler handler) {
            if(value instanceof IItemBasedValue v)
                return List.of(OverlappingItemDisplay.of(v.getAsItemList(),List.of(value.getText()),width));
            return List.of();
        }
    }

    private static class Default extends WithTextDisplay {
        @Override
        public void collectMoneyInputs(Player player,Consumer<MoneyInputHandler> builder) { }
    }

}