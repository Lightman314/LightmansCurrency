package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item;

import dan200.computercraft.api.lua.LuaException;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketItemTrade;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeType;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item.trade.ItemTradeWrapper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item.trade.TicketItemTradeWrapper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TradeOfferNodeMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ItemTradeNodeMethods extends TradeOfferNodeMethods<ItemTradeData,ItemTradeNode> {

    private static final Map<ItemTradeType<?>,BiFunction<Supplier<ItemTradeData>,Supplier<TraderData>,ItemTradeWrapper<?>>> customWrappers = new HashMap<>();

    static {
        //Register the built-in custom wrappers here
        registerCustomWrapper(TicketItemTrade.TYPE,TicketItemTradeWrapper::new);
    }

    public static void registerCustomWrapper(ItemTradeType<?> type,BiFunction<Supplier<ItemTradeData>,Supplier<TraderData>,ItemTradeWrapper<?>> factory) {
        customWrappers.put(type,factory);
    }

    public ItemTradeNodeMethods(TraderPeripheral peripheral) { super(peripheral,ItemTradeNode.TYPE); }

    @Override
    public AccessTrackingPeripheral wrapTrade(TradeData t) throws LuaException {
        if(t instanceof ItemTradeData trade)
        {
            ItemTradeNode node = this.getNode();
            int index = node.indexOfTrade(trade);
            ItemTradeType<?> type = trade.getType();
            Supplier<ItemTradeData> supplier = this.tradeSource(index);
            AccessTrackingPeripheral result;
            if(customWrappers.containsKey(type))
                result = customWrappers.get(type).apply(supplier,this::safeGetTrader);
            else
                result = new ItemTradeWrapper<>(supplier,this::safeGetTrader);
            result.setParent(this.peripheral);
            return result;
        }
        throw new LuaException("Trade is not an Item Trade!");
    }

    private Supplier<ItemTradeData> tradeSource(int index)
    {
        return () -> {
            ItemTradeNode node = this.safeGetNode();
            if(node != null && index >= 0 && index < node.getTradeCount())
                return node.getTrade(index);
            return null;
        };
    }

}
