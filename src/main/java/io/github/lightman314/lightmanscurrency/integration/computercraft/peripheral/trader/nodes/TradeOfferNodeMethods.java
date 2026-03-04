package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;

import java.util.ArrayList;
import java.util.List;

public abstract class TradeOfferNodeMethods<T extends TradeData,N extends TradeOfferSourceNode<T>> extends TraderNodeMethods<N> {

    public TradeOfferNodeMethods(TraderPeripheral peripheral, TraderNodeType<N> type) { super(peripheral,type); }

    public abstract AccessTrackingPeripheral wrapTrade(TradeData trade) throws LuaException;

    private Object getTrade(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        TraderData trader = this.getTrader();
        ArgumentHelpers.assertBetween(slot,1,trader.getTradeCount(),"Trade Slot is out of bounds (%s)");
        return this.wrapTrade(trader.getTrade(slot - 1)).asTable(computer);
    }

    private LCLuaTable getTrades(IComputerAccess compouter) throws LuaException
    {
        TraderData trader = this.getTrader();
        List<Object> results = new ArrayList<>();
        for(int i = 0; i < trader.getTradeCount(); ++i)
        {
            TradeData trade = trader.getTrade(i);
            if(trade != null)
                results.add(this.wrapTrade(trade).asTable(compouter));
        }
        return LCLuaTable.fromList(results);
    }

    @Override
    public void registerNodeMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getTrade").withContext(this::getTrade));
        registration.register(LCPeripheralMethod.builder("getTrades").withContextOnly(this::getTrades));
    }
}
