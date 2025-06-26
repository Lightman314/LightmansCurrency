package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.AccessTrackingPeripheral;

import java.util.Set;
import java.util.function.Supplier;

public abstract class TradeWrapper<T extends TradeData> extends AccessTrackingPeripheral {

    public static final String BASE_TYPE = "lc_trade";

    private final Supplier<T> source;
    private final Supplier<TraderData> trader;
    public TradeWrapper(Supplier<T> tradeSource, Supplier<TraderData> trader) { this.source = tradeSource; this.trader = trader; }

    @Override
    public Set<String> getAdditionalTypes() { return Set.of(BASE_TYPE); }

    protected final T getTrade() throws LuaException{
        T trade = this.source.get();
        if(trade == null)
            throw new LuaException("An unexpected error occurred trying to access the trade!");
        return trade;
    }

    @LuaFunction(mainThread = true)
    public boolean isValid() {
        try { return this.getTrade().isValid();
        } catch (LuaException exception) { return false; }
    }

    @LuaFunction(mainThread = true)
    public LCLuaTable getPrice() throws LuaException {
        LCLuaTable table = new LCLuaTable();
        return LCLuaTable.fromMoney(this.getTrade().getCost());
    }

    @LuaFunction(mainThread = true)
    public int getStock() throws LuaException {
        TraderData trader = this.trader.get();
        if(trader == null)
            throw new LuaException("An unexpected error occurred trying to access the trader!");
        TradeData trade = this.getTrade();
        return trade.getStock(TradeContext.createStorageMode(trader));
    }

    @LuaFunction(mainThread = true)
    public String getDirection() throws LuaException { return this.getTrade().getTradeDirection().toString(); }

    @LuaFunction(mainThread = true)
    public boolean isSale() throws LuaException { return this.getTrade().getTradeDirection() == TradeDirection.SALE; }
    @LuaFunction(mainThread = true)
    public boolean isPurchase() throws LuaException { return this.getTrade().getTradeDirection() == TradeDirection.PURCHASE; }
    @LuaFunction(mainThread = true)
    public boolean isBarter() throws LuaException { return this.getTrade().getTradeDirection() == TradeDirection.BARTER; }
    @LuaFunction(mainThread = true)
    public boolean isOther() throws LuaException { return this.getTrade().getTradeDirection() == TradeDirection.OTHER; }

}
